package top.bettercode.environment;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Listens for {@link EnvironmentChangeEvent} and rebinds beans that were bound to the
 * {@link Environment} using {@link ConfigurationProperties ConfigurationProperties}. When these beans are re-bound and
 * re-initialized, the changes are available immediately to any component that is using
 * the @ConfigurationProperties bean.
 * <p>
 *
 */
@Component
@ManagedResource
public class ConfigurationPropertiesRebinder
		implements ApplicationContextAware, ApplicationListener<EnvironmentChangeEvent> {

	private final ConfigurationPropertiesBeans beans;

	private ApplicationContext applicationContext;

	private final Map<String, Exception> errors = new ConcurrentHashMap<>();

	public ConfigurationPropertiesRebinder(ConfigurationPropertiesBeans beans) {
		this.beans = beans;
	}

	@Override
	public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * A map of bean name to errors when instantiating the bean.
	 * @return The errors accumulated since the latest destroy.
	 */
	public Map<String, Exception> getErrors() {
		return this.errors;
	}

	@ManagedOperation
	public void rebind() {
		this.errors.clear();
		for (String name : this.beans.getBeanNames()) {
			rebind(name);
		}
	}

	@ManagedOperation
	public boolean rebind(String name) {
		if (!this.beans.getBeanNames().contains(name)) {
			return false;
		}
		if (this.applicationContext != null) {
			try {
				Object bean = this.applicationContext.getBean(name);
				if (AopUtils.isAopProxy(bean)) {
					bean = ProxyUtils.getTargetObject(bean);
				}
				if (bean != null) {
					// see https://github.com/spring-cloud/spring-cloud-commons/issues/571
					if (getNeverRefreshable().contains(bean.getClass().getName())) {
						return false; // ignore
					}
					this.applicationContext.getAutowireCapableBeanFactory().destroyBean(bean);
					this.applicationContext.getAutowireCapableBeanFactory().initializeBean(bean, name);
					return true;
				}
			}
			catch (RuntimeException e) {
				this.errors.put(name, e);
				throw e;
			}
			catch (Exception e) {
				this.errors.put(name, e);
				throw new IllegalStateException("Cannot rebind to " + name, e);
			}
		}
		return false;
	}

	@ManagedAttribute
	public Set<String> getNeverRefreshable() {
		String neverRefresh = this.applicationContext.getEnvironment()
				.getProperty("spring.cloud.refresh.never-refreshable", "com.zaxxer.hikari.HikariDataSource");
		return StringUtils.commaDelimitedListToSet(neverRefresh);
	}

	@ManagedAttribute
	public Set<String> getBeanNames() {
		return new HashSet<>(this.beans.getBeanNames());
	}

	@Override
	public void onApplicationEvent(EnvironmentChangeEvent event) {
		if (this.applicationContext.equals(event.getSource())
				// Backwards compatible
				|| event.getKeys().equals(event.getSource())) {
			rebind();
		}
	}

}
