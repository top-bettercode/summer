package top.bettercode.summer.data.jpa.config

import com.zaxxer.hikari.HikariDataSource
import org.apache.ibatis.builder.xml.XMLConfigBuilder
import org.apache.ibatis.builder.xml.XMLMapperBuilder
import org.apache.ibatis.executor.ErrorContext
import org.apache.ibatis.io.Resources
import org.apache.ibatis.type.TypeHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
import top.bettercode.summer.data.jpa.BaseRepository
import top.bettercode.summer.data.jpa.JpaExtRepository
import top.bettercode.summer.data.jpa.querydsl.QuerydslRepository
import top.bettercode.summer.data.jpa.support.SimpleJpaExtRepository
import top.bettercode.summer.web.support.packagescan.PackageScanClassResolver
import java.io.IOException
import java.lang.reflect.Modifier
import java.util.*
import java.util.stream.Stream

/**
 * [Auto-Configuration][EnableAutoConfiguration] for Mybatis.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(MultiDatasourcesBeanDefinitionRegistryPostProcessor::class)
class JpaMybatisAutoConfiguration(
        private val properties: MybatisProperties,
        private val resourceLoader: ResourceLoader, @Autowired(required = false) hikari: HikariDataSource?
) : InitializingBean {
    init {
        if (hikari != null && log.isInfoEnabled) {
            log.info("init dataSource {} : {}", hikari.poolName, hikari.jdbcUrl)
        }
    }

    override fun afterPropertiesSet() {
        checkConfigFileExists()
    }

    private fun checkConfigFileExists() {
        if (properties.isCheckConfigLocation && StringUtils
                        .hasText(properties.configLocation)) {
            val resource = resourceLoader.getResource(properties.configLocation!!)
            Assert.state(resource.exists(),
                    "Cannot find config location: " + resource
                            + " (please add config file or check your Mybatis configuration)")
        }
    }

    @Bean
    fun mybatisConfiguration(beanFactory: ConfigurableListableBeanFactory): org.apache.ibatis.session.Configuration? {
        return mybatisConfiguration(beanFactory, properties.configuration, properties,
                resourceLoader,
                null)
    }

    companion object {
        private val log = LoggerFactory.getLogger(JpaMybatisAutoConfiguration::class.java)
        private val RESOURCE_PATTERN_RESOLVER: ResourcePatternResolver = PathMatchingResourcePatternResolver()
        private val METADATA_READER_FACTORY: MetadataReaderFactory = CachingMetadataReaderFactory()
        private val PACKAGE_SCAN_CLASS_RESOLVER = PackageScanClassResolver()

        fun findDefaultMapperLocations(beanFactory: ConfigurableListableBeanFactory): Set<String> {
            val packages: MutableSet<String> = HashSet()
            val beanNames = beanFactory.getBeanNamesForAnnotation(SpringBootApplication::class.java)
            for (beanName in beanNames) {
                val beanDefinition = beanFactory.getBeanDefinition(
                        beanName) as AbstractBeanDefinition
                if (!beanDefinition.hasBeanClass()) {
                    beanDefinition.resolveBeanClass(JpaMybatisAutoConfiguration::class.java.classLoader)
                }
                val beanClass = beanDefinition.beanClass
                val annotation = AnnotatedElementUtils.findMergedAnnotation(beanClass,
                        SpringBootApplication::class.java)
                for (packageClass in Objects.requireNonNull(annotation).scanBasePackageClasses) {
                    packages.add(packageClass.java.getPackage().name)
                }
                packages.addAll(listOf(*annotation!!.scanBasePackages))
                packages.add(beanClass.getPackage().name)
            }
            val implementations = PACKAGE_SCAN_CLASS_RESOLVER.findImplementations(
                    JpaExtRepository::class.java, *packages.toTypedArray<String>())
            val excludeMapperLocations: MutableSet<String> = HashSet()
            excludeMapperLocations.add(BaseRepository::class.java.getPackage().name)
            excludeMapperLocations.add(QuerydslRepository::class.java.getPackage().name)
            excludeMapperLocations.add(SimpleJpaExtRepository::class.java.getPackage().name)
            val mapperLocations: MutableSet<String> = HashSet()
            //    classpath*:/@app.packagePath@/modules/*/*/*.xml
            for (implementation in implementations) {
                val name = implementation.getPackage().name
                if (!excludeMapperLocations.contains(name)) {
                    mapperLocations.add(
                            "classpath*:/" + name.replace(".", "/") + "/*.xml")
                }
            }
            return mapperLocations
        }

        fun mybatisConfiguration(
                beanFactory: ConfigurableListableBeanFactory,
                configuration: org.apache.ibatis.session.Configuration?,
                properties: MybatisProperties,
                resourceLoader: ResourceLoader?, mapperLocations: Array<String>?
        ): org.apache.ibatis.session.Configuration {
            var configuration1 = configuration
            var mapperLocations1 = mapperLocations
            val configurationProperties = properties.configurationProperties
            var xmlConfigBuilder: XMLConfigBuilder? = null
            var configResource: Resource? = null
            if (configuration1 == null) {
                val configLocation = properties.configLocation
                if (StringUtils.hasText(configLocation)) {
                    configResource = resourceLoader!!.getResource(configLocation!!)
                    xmlConfigBuilder = XMLConfigBuilder(configResource.inputStream, null,
                            configurationProperties)
                    configuration1 = xmlConfigBuilder.configuration
                } else {
                    if (log.isDebugEnabled) {
                        log.debug(
                                "Property 'configuration' or 'configLocation' not specified, using default MyBatis Configuration")
                    }
                    configuration1 = org.apache.ibatis.session.Configuration()
                    Optional.ofNullable(configurationProperties).ifPresent { variables: Properties? -> configuration1.variables = variables }
                }
            }
            val typeAliasesPackage = properties.typeAliasesPackage
            if (StringUtils.hasText(typeAliasesPackage)) {
                scanClasses(typeAliasesPackage, properties.typeAliasesSuperType).stream()
                        .filter { clazz: Class<*> -> !clazz.isAnonymousClass }.filter { clazz: Class<*> -> !clazz.isInterface }
                        .filter { clazz: Class<*> -> !clazz.isMemberClass }
                        .forEach { type: Class<*>? -> configuration1!!.typeAliasRegistry.registerAlias(type) }
            }
            val typeAliases = properties.typeAliases
            val finalConfiguration = configuration1
            if (typeAliases.isNotEmpty()) {
                Stream.of(*typeAliases).forEach { typeAlias: Class<*>? ->
                    finalConfiguration!!.typeAliasRegistry.registerAlias(typeAlias)
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
                        .forEach { typeHandlerClass: Class<*>? -> configuration1!!.typeHandlerRegistry.register(typeHandlerClass) }
            }
            val typeHandlerClasses = properties.typeHandlerClasses
            if (typeHandlerClasses.isNotEmpty()) {
                Stream.of(*typeHandlerClasses).forEach { typeHandler: Class<TypeHandler<*>>? ->
                    finalConfiguration!!.typeHandlerRegistry.register(typeHandler)
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
            if (mapperLocations1.isNullOrEmpty()) {
                mapperLocations1 = properties.mapperLocations
            }
            if (mapperLocations1.isEmpty()) {
                val defaultMapperLocations = findDefaultMapperLocations(
                        beanFactory)
                mapperLocations1 = defaultMapperLocations.toTypedArray()
            }
            val mapperResources = resolveMapperLocations(mapperLocations1)
            if (configuration1!!.variables == null) {
                configuration1.variables = configurationProperties
            } else if (configurationProperties != null) {
                configuration1.variables.putAll(configurationProperties)
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
                                configuration1, mapperResource.toString(), configuration1.sqlFragments)
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
            return configuration1
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
}
