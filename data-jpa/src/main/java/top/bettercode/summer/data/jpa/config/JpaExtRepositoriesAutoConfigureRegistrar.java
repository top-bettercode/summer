package top.bettercode.summer.data.jpa.config;

import java.lang.annotation.Annotation;
import java.util.Locale;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.util.StringUtils;

/**
 * {@link ImportBeanDefinitionRegistrar} used to auto-configure Spring Data JPA
 * Repositories.
 *
 * @author Phillip Webb
 * @author Dave Syer
 */
class JpaExtRepositoriesAutoConfigureRegistrar
		extends AbstractRepositoryConfigurationSourceSupport {

	private BootstrapMode bootstrapMode = null;

	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableJpaExtRepositories.class;
	}

	@Override
	protected Class<?> getConfiguration() {
		return EnableJpaExtRepositoriesConfiguration.class;
	}

	@Override
	protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
		return new JpaExtRepositoryConfigExtension();
	}

	@Override
	protected BootstrapMode getBootstrapMode() {
		return (this.bootstrapMode == null) ? super.getBootstrapMode()
				: this.bootstrapMode;
	}

	@Override
	public void setEnvironment(Environment environment) {
		super.setEnvironment(environment);
		configureBootstrapMode(environment);
	}

	private void configureBootstrapMode(Environment environment) {
		String property = environment
				.getProperty("spring.data.jpa.repositories.bootstrap-mode");
		if (StringUtils.hasText(property)) {
			this.bootstrapMode = BootstrapMode
					.valueOf(property.toUpperCase(Locale.ENGLISH));
		}
	}

	@EnableJpaExtRepositories
	private static class EnableJpaExtRepositoriesConfiguration {

	}

}