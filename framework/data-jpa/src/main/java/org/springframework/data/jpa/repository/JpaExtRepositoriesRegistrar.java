package org.springframework.data.jpa.repository;

import top.bettercode.simpleframework.data.jpa.config.JpaExtRepositoryConfigExtension;
import java.lang.annotation.Annotation;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

class JpaExtRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

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