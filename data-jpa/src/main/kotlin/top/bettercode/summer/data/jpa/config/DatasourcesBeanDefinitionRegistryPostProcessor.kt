package top.bettercode.summer.data.jpa.config

import com.zaxxer.hikari.HikariDataSource
import org.apache.ibatis.session.Configuration
import org.hibernate.boot.model.naming.ImplicitNamingStrategy
import org.hibernate.boot.model.naming.PhysicalNamingStrategy
import org.hibernate.cfg.AvailableSettings
import org.slf4j.LoggerFactory
import org.springframework.aop.framework.autoproxy.AutoProxyUtils
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.EnvironmentAware
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader
import org.springframework.orm.hibernate5.SpringBeanContainer
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.util.Assert
import org.springframework.util.ClassUtils
import org.springframework.util.ObjectUtils
import java.util.stream.Collectors
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource
import kotlin.String

class DatasourcesBeanDefinitionRegistryPostProcessor : BeanDefinitionRegistryPostProcessor, ResourceLoaderAware, EnvironmentAware {
    private val log = LoggerFactory.getLogger(
            DatasourcesBeanDefinitionRegistryPostProcessor::class.java)
    private lateinit var resourceLoader: ResourceLoader
    private lateinit var environment: Environment

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val dataSources = Binder.get(
                environment).bind("summer.datasource.multi.datasources", Bindable
                .mapOf(String::class.java, DataSourceExtProperties::class.java)).orElse(null)
        val configurationSources = JpaMybatisConfigurationUtil.findConfigurationSources(beanFactory)
        if (configurationSources.size > 1) {
            val factory = beanFactory as DefaultListableBeanFactory
            configurationSources.forEach { (key, configurationSource) ->
                val properties = dataSources[key]
                        ?: throw RuntimeException("$key datasource config not found")
                val jpaExtRepositories = configurationSource.annotation
                val basePackages = JpaMybatisConfigurationUtil.getBasePackages(configurationSource)
                val primary = "primary" == key
                val dataSourceBeanName = if (primary) "dataSource" else key + "DataSource"
                var beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
                        HikariDataSource::class.java) {
                    val dataSource = properties.initializeDataSourceBuilder().type(HikariDataSource::class.java).build()
                    if (!properties.name.isNullOrBlank()) {
                        dataSource.poolName = properties.name
                    }
                    if (dataSource.poolName.isNullOrBlank()) {
                        dataSource.poolName = "${key}Pool"
                    }

                    Binder.get(environment).bind("spring.datasource.hikari", Bindable.ofInstance(dataSource))
                    val hikariConfigKey = "summer.datasource.multi.datasources.$key.hikari"
                    Binder.get(environment).bind(hikariConfigKey, Bindable.ofInstance(dataSource))

                    if (log.isInfoEnabled) {
                        log.info("init dataSource {} : {}", dataSource.poolName,
                                dataSource.jdbcUrl)
                    }
                    dataSource
                }
                if (primary) {
                    beanDefinitionBuilder.setPrimary(true)
                    if (factory.containsBeanDefinition(dataSourceBeanName)) {
                        factory.removeBeanDefinition(dataSourceBeanName)
                    }
                }
                var beanDefinition = beanDefinitionBuilder.beanDefinition
                beanDefinition.isSynthetic = true
                beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, true)
                factory.registerBeanDefinition(dataSourceBeanName, beanDefinition)

                //entityManagerFactory
                val entityManagerFactoryBeanName = jpaExtRepositories.entityManagerFactoryRef
                beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
                        LocalContainerEntityManagerFactoryBean::class.java) {
                    val builder = beanFactory.getBean(EntityManagerFactoryBuilder::class.java)
                    val dataSource = beanFactory.getBean(dataSourceBeanName, DataSource::class.java)
                    val hibernateProperties = beanFactory.getBean(HibernateProperties::class.java)
                    val jpaProperties = beanFactory.getBean(JpaProperties::class.java)
                    val mappingResourceList = jpaProperties.mappingResources
                    val mappingResources = if (!ObjectUtils.isEmpty(mappingResourceList)) mappingResourceList.toTypedArray() else emptyArray()
                    val physicalNamingStrategy = beanFactory.getBeanProvider(PhysicalNamingStrategy::class.java)
                    val implicitNamingStrategy = beanFactory.getBeanProvider(
                            ImplicitNamingStrategy::class.java)
                    val hibernatePropertiesCustomizers = determineHibernatePropertiesCustomizers(
                            physicalNamingStrategy.getIfAvailable(), implicitNamingStrategy.getIfAvailable(),
                            beanFactory,
                            beanFactory.getBeanProvider(HibernatePropertiesCustomizer::class.java).orderedStream()
                                    .collect(Collectors.toList()))
                    val vendorProperties = LinkedHashMap(
                            hibernateProperties
                                    .determineHibernateProperties(jpaProperties.properties,
                                            HibernateSettings()
                                                    .hibernatePropertiesCustomizers(hibernatePropertiesCustomizers)
                                    ))
                    builder
                            .dataSource(dataSource)
                            .properties(vendorProperties)
                            .packages(*basePackages)
                            .mappingResources(*mappingResources)
                            .build()
                }
                if (primary) {
                    beanDefinitionBuilder.setPrimary(true)
                    if (factory.containsBeanDefinition(entityManagerFactoryBeanName)) {
                        factory.removeBeanDefinition(entityManagerFactoryBeanName)
                    }
                }
                beanDefinitionBuilder.addDependsOn(dataSourceBeanName)
                beanDefinition = beanDefinitionBuilder.beanDefinition
                beanDefinition.isSynthetic = true
                beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, true)
                factory.registerBeanDefinition(entityManagerFactoryBeanName, beanDefinition)

                //transactionManager
                val transactionManagerBeanName = jpaExtRepositories.transactionManagerRef
                beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(PlatformTransactionManager::class.java) {
                    val entityManagerFactory = beanFactory.getBean(
                            entityManagerFactoryBeanName,
                            EntityManagerFactory::class.java)
                    val jpaTransactionManager = JpaTransactionManager(
                            entityManagerFactory)
                    val transactionManagerCustomizers = beanFactory.getBeanProvider(
                            TransactionManagerCustomizers::class.java)
                    transactionManagerCustomizers.ifAvailable { customizers: TransactionManagerCustomizers -> customizers.customize(jpaTransactionManager) }
                    jpaTransactionManager
                }
                if (primary) {
                    beanDefinitionBuilder.setPrimary(true)
                    if (factory.containsBeanDefinition(transactionManagerBeanName)) {
                        factory.removeBeanDefinition(transactionManagerBeanName)
                    }
                }
                beanDefinitionBuilder.addDependsOn(entityManagerFactoryBeanName)
                beanDefinition = beanDefinitionBuilder.beanDefinition
                beanDefinition.isSynthetic = true
                beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, true)
                factory.registerBeanDefinition(transactionManagerBeanName, beanDefinition)

                // mybatisConfiguration
                val mybatisConfigurationRef = jpaExtRepositories.mybatisConfigurationRef
                beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Configuration::class.java) {
                    val mybatisProperties = beanFactory.getBean(MybatisProperties::class.java)
                    return@genericBeanDefinition JpaMybatisConfigurationUtil.mybatisConfiguration(
                            mybatisProperties, resourceLoader, properties.mapperLocations + mybatisProperties.mapperLocations, basePackages)
                }
                if (primary) {
                    beanDefinitionBuilder.setPrimary(true)
                    if (factory.containsBeanDefinition(mybatisConfigurationRef)) {
                        factory.removeBeanDefinition(mybatisConfigurationRef)
                    }
                }
                beanDefinition = beanDefinitionBuilder.beanDefinition
                beanDefinition.isSynthetic = true
                beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, true)
                factory.registerBeanDefinition(mybatisConfigurationRef, beanDefinition)
            }
        } else {
            Assert.isTrue(configurationSources.size == 1, "未配置 @EnableJpaExtRepositories")
            val factory = beanFactory as DefaultListableBeanFactory
            val configurationSource = configurationSources.values.first()
            val jpaExtRepositories = configurationSource.annotation

            // mybatisConfiguration
            val basePackages = JpaMybatisConfigurationUtil.getBasePackages(configurationSource)
            val mybatisConfigurationRef = jpaExtRepositories.mybatisConfigurationRef
            val beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Configuration::class.java) {
                val dataSource = beanFactory.getBean(DataSource::class.java)
                if (log.isInfoEnabled) {
                    if (dataSource is HikariDataSource) {
                        if (dataSource.poolName.isNullOrBlank()) {
                            dataSource.poolName = "${jpaExtRepositories.key}Pool"
                        }
                        log.info("init dataSource {} : {}", dataSource.poolName, dataSource.jdbcUrl)
                    }
                }
                val mybatisProperties = beanFactory.getBean(MybatisProperties::class.java)
                return@genericBeanDefinition JpaMybatisConfigurationUtil.mybatisConfiguration(
                        mybatisProperties, resourceLoader, jpaExtRepositories.mapperLocations, basePackages)
            }
            beanDefinitionBuilder.setPrimary(true)
            val beanDefinition = beanDefinitionBuilder.beanDefinition
            beanDefinition.isSynthetic = true
            beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, true)
            factory.registerBeanDefinition(mybatisConfigurationRef, beanDefinition)
        }
    }

    private fun determineHibernatePropertiesCustomizers(
            physicalNamingStrategy: PhysicalNamingStrategy?, implicitNamingStrategy: ImplicitNamingStrategy?,
            beanFactory: ConfigurableListableBeanFactory,
            hibernatePropertiesCustomizers: List<HibernatePropertiesCustomizer>
    ): List<HibernatePropertiesCustomizer> {
        val customizers: MutableList<HibernatePropertiesCustomizer> = ArrayList()
        if (ClassUtils.isPresent("org.hibernate.resource.beans.container.spi.BeanContainer",
                        javaClass.classLoader)) {
            customizers.add(HibernatePropertiesCustomizer { properties: MutableMap<String?, Any?> -> properties[AvailableSettings.BEAN_CONTAINER] = SpringBeanContainer(beanFactory) })
        }
        if (physicalNamingStrategy != null || implicitNamingStrategy != null) {
            customizers.add(
                    NamingStrategiesHibernatePropertiesCustomizer(physicalNamingStrategy,
                            implicitNamingStrategy))
        }
        customizers.addAll(hibernatePropertiesCustomizers)
        return customizers
    }

    private class NamingStrategiesHibernatePropertiesCustomizer(
            private val physicalNamingStrategy: PhysicalNamingStrategy?,
            private val implicitNamingStrategy: ImplicitNamingStrategy?
    ) : HibernatePropertiesCustomizer {
        override fun customize(hibernateProperties: MutableMap<String, Any>) {
            if (physicalNamingStrategy != null) {
                hibernateProperties["hibernate.physical_naming_strategy"] = physicalNamingStrategy
            }
            if (implicitNamingStrategy != null) {
                hibernateProperties["hibernate.implicit_naming_strategy"] = implicitNamingStrategy
            }
        }
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
    }

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }
}