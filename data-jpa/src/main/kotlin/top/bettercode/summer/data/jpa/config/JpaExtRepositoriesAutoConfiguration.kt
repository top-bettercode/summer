package top.bettercode.summer.data.jpa.config

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.*
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilderCustomizer
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase
import org.springframework.context.annotation.Import
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.data.jpa.repository.JpaRepository
import top.bettercode.summer.data.jpa.support.JpaExtRepositoryFactoryBean
import javax.sql.DataSource

/**
 * DataJpaConfiguration 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(DataSource::class)
@ConditionalOnClass(JpaRepository::class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnMissingBean(JpaExtRepositoryFactoryBean::class, JpaExtRepositoryConfigExtension::class)
@ConditionalOnProperty(prefix = "spring.data.jpa.repositories", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@Import(JpaExtRepositoriesAutoConfigureRegistrar::class)
@AutoConfigureAfter(HibernateJpaAutoConfiguration::class, TaskExecutionAutoConfiguration::class)
class JpaExtRepositoriesAutoConfiguration {
    @Bean
    @Conditional(BootstrapExecutorCondition::class)
    fun entityManagerFactoryBootstrapExecutorCustomizer(
            taskExecutor: ObjectProvider<AsyncTaskExecutor?>
    ): EntityManagerFactoryBuilderCustomizer {
        return EntityManagerFactoryBuilderCustomizer { builder: EntityManagerFactoryBuilder -> builder.setBootstrapExecutor(taskExecutor.getIfAvailable()) }
    }

    private class BootstrapExecutorCondition : AnyNestedCondition(ConfigurationPhase.REGISTER_BEAN) {
        @ConditionalOnProperty(prefix = "spring.data.jpa.repositories", name = ["bootstrap-mode"], havingValue = "deferred")
        class DeferredBootstrapMode

        @ConditionalOnProperty(prefix = "spring.data.jpa.repositories", name = ["bootstrap-mode"], havingValue = "lazy")
        class LazyBootstrapMode
    }
}
