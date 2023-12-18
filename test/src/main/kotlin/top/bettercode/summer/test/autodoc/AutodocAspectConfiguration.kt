package top.bettercode.summer.test.autodoc

import jakarta.persistence.EntityManagerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext

/**
 *
 * @author Peter Wu
 */
@ConditionalOnProperty(prefix = "summer.autodoc.gen", name = ["enable"], havingValue = "true")
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@ConditionalOnClass(EntityManagerFactory::class)
@ConditionalOnBean(EntityManagerFactory::class)
class AutodocAspectConfiguration {

    @Bean
    fun autoDocAspect(
        entityManagerFactorys: MutableList<EntityManagerFactory>,
        applicationContext: GenericApplicationContext
    ): AutodocAspect {
        return AutodocAspect(entityManagerFactorys, applicationContext)
    }


}