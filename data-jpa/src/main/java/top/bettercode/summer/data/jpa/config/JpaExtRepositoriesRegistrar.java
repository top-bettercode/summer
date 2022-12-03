package top.bettercode.summer.data.jpa.config;

import java.lang.annotation.Annotation;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

public class JpaExtRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getAnnotation()
   */
  @NotNull
  @Override
  protected Class<? extends Annotation> getAnnotation() {
    return EnableJpaExtRepositories.class;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getExtension()
   */
  @NotNull
  @Override
  protected RepositoryConfigurationExtension getExtension() {
    return new JpaExtRepositoryConfigExtension();
  }
}