package top.bettercode.simpleframework.data.jpa.config;

import java.lang.annotation.Annotation;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

public class JpaExtRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getAnnotation()
   */
  @Override
  protected Class<? extends Annotation> getAnnotation() {
    return EnableJpaExtRepositories.class;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getExtension()
   */
  @Override
  protected RepositoryConfigurationExtension getExtension() {
    return new JpaExtRepositoryConfigExtension();
  }
}