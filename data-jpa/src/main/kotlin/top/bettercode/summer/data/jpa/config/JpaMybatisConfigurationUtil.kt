package top.bettercode.summer.data.jpa.config

import org.apache.ibatis.builder.xml.XMLConfigBuilder
import org.apache.ibatis.builder.xml.XMLMapperBuilder
import org.apache.ibatis.executor.ErrorContext
import org.apache.ibatis.io.Resources
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.type.TypeHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.core.type.classreading.CachingMetadataReaderFactory
import org.springframework.core.type.classreading.MetadataReaderFactory
import org.springframework.util.Assert
import org.springframework.util.ClassUtils
import org.springframework.util.StringUtils
import top.bettercode.summer.data.jpa.JpaExtRepository
import top.bettercode.summer.web.support.packagescan.PackageScanClassResolver
import java.io.IOException
import java.lang.reflect.Modifier
import java.util.*
import java.util.stream.Stream
import kotlin.reflect.KClass


object JpaMybatisConfigurationUtil {
    private val log = LoggerFactory.getLogger(JpaMybatisConfigurationUtil::class.java)
    private val RESOURCE_PATTERN_RESOLVER: ResourcePatternResolver = PathMatchingResourcePatternResolver()
    private val METADATA_READER_FACTORY: MetadataReaderFactory = CachingMetadataReaderFactory()
    private val PACKAGE_SCAN_CLASS_RESOLVER = PackageScanClassResolver()

    fun findConfigurationSources(beanFactory: ConfigurableListableBeanFactory): Map<String, ExtRepositoryConfigurationSource> {
        val beanNames = beanFactory.getBeanNamesForAnnotation(EnableJpaExtRepositories::class.java)
        val keys = mutableSetOf<String>()
        return beanNames.associate {
            val beanDefinition = beanFactory.getBeanDefinition(it) as AbstractBeanDefinition
            if (!beanDefinition.hasBeanClass()) {
                beanDefinition.resolveBeanClass(JpaMybatisConfigurationUtil::class.java.classLoader)
            }
            val beanClass = beanDefinition.beanClass
            val annotation = AnnotatedElementUtils.findMergedAnnotation(beanClass, EnableJpaExtRepositories::class.java)!!
            val key = annotation.key
            Assert.isTrue(!keys.contains(key), "Duplicate repositoryConfiguration key $key")
            keys.add(key)
            Pair(key, ExtRepositoryConfigurationSource(beanClass, annotation))
        }
    }

    fun getBasePackages(source: ExtRepositoryConfigurationSource): Array<String> {
        val configClass: Class<*> = source.configClass
        val anno: EnableJpaExtRepositories = source.annotation
        val value = anno.value
        val basePackages = anno.basePackages
        val basePackageClasses = anno.basePackageClasses

        // Default configuration - return package of annotated class
        if (value.isEmpty() && basePackages.isEmpty() && basePackageClasses.isEmpty()) {
            return arrayOf(ClassUtils.getPackageName(configClass))
        }
        val packages: MutableSet<String> = HashSet()
        packages.addAll(value)
        packages.addAll(basePackages)
        packages.addAll(basePackageClasses
                .map { clazz: KClass<*> -> ClassUtils.getPackageName(clazz.java) })
        return packages.toTypedArray()
    }

    fun mybatisConfiguration(
            properties: MybatisProperties,
            resourceLoader: ResourceLoader,
            mapperLocations: Array<String>,
            packages: Array<String>
    ): Configuration {
        var mapperLocationsTmp = properties.mapperLocations + mapperLocations
        val configurationProperties = properties.configurationProperties
        var xmlConfigBuilder: XMLConfigBuilder? = null
        var configResource: Resource? = null
        val mybatisConfiguration: Configuration =
                if (properties.configuration == null) {
                    val configLocation = properties.configLocation
                    if (StringUtils.hasText(configLocation)) {
                        configResource = resourceLoader.getResource(configLocation!!)
                        xmlConfigBuilder = XMLConfigBuilder(configResource.inputStream, null,
                                configurationProperties)
                        xmlConfigBuilder.configuration
                    } else {
                        if (log.isDebugEnabled) {
                            log.debug(
                                    "Property 'configuration' or 'configLocation' not specified, using default MyBatis Configuration")
                        }
                        val newConfiguration = Configuration()
                        configurationProperties?.let { variables: Properties -> newConfiguration.variables = variables }
                        newConfiguration
                    }
                } else {
                    val newConfiguration = Configuration()
                    BeanUtils.copyProperties(properties.configuration!!, newConfiguration)
                    newConfiguration
                }
        val typeAliasesPackage = properties.typeAliasesPackage
        if (StringUtils.hasText(typeAliasesPackage)) {
            scanClasses(typeAliasesPackage, properties.typeAliasesSuperType).stream()
                    .filter { clazz: Class<*> -> !clazz.isAnonymousClass }.filter { clazz: Class<*> -> !clazz.isInterface }
                    .filter { clazz: Class<*> -> !clazz.isMemberClass }
                    .forEach { type: Class<*> -> mybatisConfiguration.typeAliasRegistry.registerAlias(type) }
        }
        val typeAliases = properties.typeAliases
        if (typeAliases.isNotEmpty()) {
            Stream.of(*typeAliases).forEach { typeAlias: Class<*> ->
                mybatisConfiguration.typeAliasRegistry.registerAlias(typeAlias)
                if (log.isTraceEnabled) {
                    log.trace("Registered type alias: '$typeAlias'")
                }
            }
        }
        val typeHandlersPackage = properties.typeHandlersPackage
        if (StringUtils.hasText(typeHandlersPackage)) {
            scanClasses(typeHandlersPackage, TypeHandler::class.java).stream()
                    .filter { clazz: Class<*> -> !clazz.isAnonymousClass }
                    .filter { clazz: Class<*> -> !clazz.isInterface }
                    .filter { clazz: Class<*> -> !Modifier.isAbstract(clazz.modifiers) }
                    .forEach { typeHandlerClass: Class<*> -> mybatisConfiguration.typeHandlerRegistry.register(typeHandlerClass) }
        }
        val typeHandlerClasses = properties.typeHandlerClasses
        if (typeHandlerClasses.isNotEmpty()) {
            typeHandlerClasses.forEach { typeHandler: Class<TypeHandler<*>> ->
                mybatisConfiguration.typeHandlerRegistry.register(typeHandler)
                if (log.isTraceEnabled) {
                    log.trace("Registered type handler: '$typeHandler'")
                }
            }
        }
        if (xmlConfigBuilder != null) {
            try {
                xmlConfigBuilder.parse()
                if (log.isTraceEnabled) {
                    log.trace("Parsed configuration file: '$configResource'")
                }
            } catch (ex: Exception) {
                throw IOException("Failed to parse config resource: $configResource", ex)
            } finally {
                ErrorContext.instance().reset()
            }
        }

        if (mapperLocationsTmp.isEmpty()) {
            val locations: MutableSet<String> = HashSet()
            //    classpath*:/@app.packagePath@/modules/*/*/*.xml
            val implementations = PACKAGE_SCAN_CLASS_RESOLVER.findImplementations(JpaExtRepository::class.java, *packages)
            for (implementation in implementations) {
                mybatisConfiguration.addMapper(implementation)
                val name = implementation.getPackage().name
                locations.add("classpath*:/" + name.replace(".", "/") + "/*.xml")
            }
            mapperLocationsTmp = locations.toTypedArray()
        } else {
            val implementations = PACKAGE_SCAN_CLASS_RESOLVER.findImplementations(JpaExtRepository::class.java, *packages)
            for (implementation in implementations) {
                mybatisConfiguration.addMapper(implementation)
            }
        }
        val mapperResources = resolveMapperLocations(mapperLocationsTmp)
        if (mybatisConfiguration.variables == null) {
            mybatisConfiguration.variables = configurationProperties
        } else if (configurationProperties != null) {
            mybatisConfiguration.variables.putAll(configurationProperties)
        }
        if (mapperResources.isEmpty()) {
            if (log.isInfoEnabled) {
                log.info(
                        "Property 'mapperLocations' was specified but matching resources are not found.")
            }
        } else {
            for (mapperResource in mapperResources) {
                if (mapperResource == null) {
                    continue
                }
                try {
                    val xmlMapperBuilder = XMLMapperBuilder(
                            mapperResource.inputStream,
                            mybatisConfiguration, mapperResource.toString(), mybatisConfiguration.sqlFragments)
                    xmlMapperBuilder.parse()
                } catch (e: Exception) {
                    throw IOException(
                            "Failed to parse mapping resource: '$mapperResource'", e)
                } finally {
                    ErrorContext.instance().reset()
                }
                if (log.isTraceEnabled) {
                    log.trace("Parsed mapper file: '$mapperResource'")
                }
            }
        }
        return mybatisConfiguration
    }

    fun resolveMapperLocations(mapperLocations: Array<String>): Array<Resource?> {
        return mapperLocations.flatMap { getResources(it).toList() }.toTypedArray()

    }

    private fun getResources(location: String): Array<Resource?> {
        return try {
            RESOURCE_PATTERN_RESOLVER.getResources(location)
        } catch (e: IOException) {
            arrayOf()
        }
    }

    private fun scanClasses(packagePatterns: String?, assignableType: Class<*>?): Set<Class<*>> {
        val classes: MutableSet<Class<*>> = HashSet()
        val packagePatternArray = StringUtils.tokenizeToStringArray(packagePatterns,
                ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS)
        for (packagePattern in packagePatternArray) {
            val resources = RESOURCE_PATTERN_RESOLVER.getResources(
                    ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                            + ClassUtils.convertClassNameToResourcePath(packagePattern) + "/**/*.class")
            for (resource in resources) {
                try {
                    val classMetadata = METADATA_READER_FACTORY.getMetadataReader(resource)
                            .classMetadata
                    val clazz = Resources.classForName(classMetadata.className)
                    if (assignableType == null || assignableType.isAssignableFrom(clazz)) {
                        classes.add(clazz)
                    }
                } catch (e: Throwable) {
                    if (log.isWarnEnabled) {
                        log.warn("Cannot load the '$resource'. Cause by $e")
                    }
                }
            }
        }
        return classes
    }
}
