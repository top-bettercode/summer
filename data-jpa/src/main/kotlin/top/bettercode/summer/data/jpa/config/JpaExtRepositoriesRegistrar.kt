package top.bettercode.summer.data.jpa.config

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport
import org.springframework.data.repository.config.RepositoryConfigurationExtension

class JpaExtRepositoriesRegistrar : RepositoryBeanDefinitionRegistrarSupport() {
    /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getAnnotation()
   */
    override fun getAnnotation(): Class<out Annotation?> {
        return EnableJpaExtRepositories::class.java
    }

    /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getExtension()
   */
    override fun getExtension(): RepositoryConfigurationExtension {
        return JpaExtRepositoryConfigExtension()
    }
}