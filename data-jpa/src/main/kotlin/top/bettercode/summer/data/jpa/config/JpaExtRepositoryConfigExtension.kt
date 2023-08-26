package top.bettercode.summer.data.jpa.config

import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension
import org.springframework.data.repository.config.RepositoryConfigurationSource

/**
 * @author Peter Wu
 */
class JpaExtRepositoryConfigExtension : JpaRepositoryConfigExtension() {
    override fun postProcess(
            builder: BeanDefinitionBuilder, source: RepositoryConfigurationSource
    ) {
        super.postProcess(builder, source)
        builder.addPropertyReference("mybatisConfiguration",
                source.getAttribute("mybatisConfigurationRef").orElse("mybatisConfiguration"))
        builder.addPropertyReference("jpaExtProperties",
                source.getAttribute("jpaExtPropertiesRef").orElse("jpaExtProperties"))
        builder.addAutowiredProperty("auditorAware")
    }
}
