package top.bettercode.summer.test.autodoc

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManagerFactory

/**
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@ConditionalOnClass(EntityManagerFactory::class)
@ConditionalOnBean(EntityManagerFactory::class)
class AutodocAspectConfiguration {

    @Bean
    fun autoDocAspect(entityManagerFactory: EntityManagerFactory): AutodocAspect {
        return AutodocAspect(entityManagerFactory)
    }


}