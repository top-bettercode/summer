package top.bettercode.summer.data.jpa.support

import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.type.TypeHandler
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.SqlSessionTemplate
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.util.Assert
import org.springframework.util.ObjectUtils
import org.springframework.util.StringUtils
import top.bettercode.summer.data.jpa.config.JpaMybatisAutoConfiguration
import top.bettercode.summer.data.jpa.config.MybatisProperties
import java.util.stream.Stream
import javax.sql.DataSource

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DataSourceAutoConfiguration::class)
class TestMybatisAutoConfiguration(
        private val properties: MybatisProperties,
        private val resourceLoader: ResourceLoader
) : InitializingBean {
    override fun afterPropertiesSet() {
        checkConfigFileExists()
    }

    private fun checkConfigFileExists() {
        if (properties.checkConfigLocation && StringUtils
                        .hasText(properties.configLocation)) {
            val resource = resourceLoader.getResource(properties.configLocation!!)
            Assert.state(resource.exists(),
                    "Cannot find config location: " + resource
                            + " (please add config file or check your Mybatis configuration)")
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @Throws(Exception::class)
    fun sqlSessionFactory(dataSource: DataSource?): SqlSessionFactory? {
        val factory = SqlSessionFactoryBean()
        factory.setDataSource(dataSource)
        val configLocation = properties.configLocation
        if (StringUtils.hasText(configLocation)) {
            factory.setConfigLocation(resourceLoader.getResource(configLocation!!))
        }
        val configuration = properties.configuration!!
        factory.setConfiguration(configuration)
        factory.setConfigurationProperties(properties.configurationProperties)
        factory.setTypeAliasesPackage(properties.typeAliasesPackage)
        factory.setTypeAliases(*properties.typeAliases)
        factory.setTypeAliasesSuperType(properties.typeAliasesSuperType)
        factory.setTypeHandlersPackage(properties.typeHandlersPackage)
        val typeHandlerClasses = properties.typeHandlerClasses
        if (typeHandlerClasses.isNotEmpty()) {
            Stream.of(*typeHandlerClasses)
                    .forEach { typeHandler: Class<TypeHandler<*>>? -> configuration.typeHandlerRegistry.register(typeHandler) }
        }
        val mapperLocations = JpaMybatisAutoConfiguration.resolveMapperLocations(
                properties.mapperLocations)
        if (!ObjectUtils.isEmpty(mapperLocations)) {
            factory.setMapperLocations(*mapperLocations)
        }
        return factory.getObject()
    }

    @Bean("sqlSessionTemplate")
    @ConditionalOnMissingBean
    fun sqlSessionTemplate(sqlSessionFactory: SqlSessionFactory?): SqlSessionTemplate {
        return SqlSessionTemplate(sqlSessionFactory)
    }
}
