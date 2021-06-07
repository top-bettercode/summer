package top.bettercode.simpleframework.data.jpa.config;

import top.bettercode.simpleframework.data.jpa.support.JpaExtRepositoryFactoryBean;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilderCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DataJpaConfiguration 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(DataSource.class)
@ConditionalOnClass(JpaRepository.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnMissingBean({JpaExtRepositoryFactoryBean.class,
    JpaExtRepositoryConfigExtension.class})
@ConditionalOnProperty(prefix = "spring.data.jpa.repositories", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(JpaExtRepositoriesAutoConfigureRegistrar.class)
@AutoConfigureAfter({HibernateJpaAutoConfiguration.class,
    TaskExecutionAutoConfiguration.class})
public class JpaExtRepositoriesAutoConfiguration {

  @Bean
  @Conditional(BootstrapExecutorCondition.class)
  public EntityManagerFactoryBuilderCustomizer entityManagerFactoryBootstrapExecutorCustomizer(
      ObjectProvider<AsyncTaskExecutor> taskExecutor) {
    return (builder) -> builder.setBootstrapExecutor(taskExecutor.getIfAvailable());
  }

  private static final class BootstrapExecutorCondition extends AnyNestedCondition {

    BootstrapExecutorCondition() {
      super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnProperty(prefix = "spring.data.jpa.repositories", name = "bootstrap-mode", havingValue = "deferred")
    static class DeferredBootstrapMode {

    }

    @ConditionalOnProperty(prefix = "spring.data.jpa.repositories", name = "bootstrap-mode", havingValue = "lazy")
    static class LazyBootstrapMode {

    }

  }

}
