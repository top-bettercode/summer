package top.bettercode.summer.data.jpa.config

import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport
import org.springframework.core.env.Environment
import org.springframework.data.repository.config.BootstrapMode
import org.springframework.data.repository.config.RepositoryConfigurationExtension
import org.springframework.util.StringUtils

/**
 * [ImportBeanDefinitionRegistrar] used to auto-configure Spring Data JPA Repositories.
 *
 * @author Phillip Webb
 * @author Dave Syer
 */
internal class JpaExtRepositoriesAutoConfigureRegistrar : AbstractRepositoryConfigurationSourceSupport() {
    private var bootstrapMode: BootstrapMode? = null
    override fun getAnnotation(): Class<out Annotation?> {
        return EnableJpaExtRepositories::class.java
    }

    override fun getConfiguration(): Class<*> {
        return EnableJpaExtRepositoriesConfiguration::class.java
    }

    override fun getRepositoryConfigurationExtension(): RepositoryConfigurationExtension {
        return JpaExtRepositoryConfigExtension()
    }

    override fun getBootstrapMode(): BootstrapMode {
        return if (bootstrapMode == null) super.getBootstrapMode() else bootstrapMode!!
    }

    override fun setEnvironment(environment: Environment) {
        super.setEnvironment(environment)
        configureBootstrapMode(environment)
    }

    private fun configureBootstrapMode(environment: Environment) {
        val property = environment
                .getProperty("spring.data.jpa.repositories.bootstrap-mode")
        if (StringUtils.hasText(property)) {
            bootstrapMode = BootstrapMode
                    .valueOf(property!!.toUpperCase())
        }
    }

    @EnableJpaExtRepositories
    private class EnableJpaExtRepositoriesConfiguration
}