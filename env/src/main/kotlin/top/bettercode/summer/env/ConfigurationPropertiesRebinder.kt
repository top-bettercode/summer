package top.bettercode.summer.env

import org.springframework.aop.support.AopUtils
import org.springframework.beans.BeansException
import org.springframework.context.*
import org.springframework.jmx.export.annotation.ManagedAttribute
import org.springframework.jmx.export.annotation.ManagedOperation
import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.util.concurrent.ConcurrentHashMap

/**
 * Listens for [EnvironmentChangeEvent] and rebinds beans that were bound to the
 * [Environment] using [ConfigurationProperties]. When these
 * beans are re-bound and re-initialized, the changes are available immediately to any component
 * that is using the @ConfigurationProperties bean.
 *
 *
 */
@Component
@ManagedResource
class ConfigurationPropertiesRebinder(private val beans: ConfigurationPropertiesBeans) : ApplicationContextAware, ApplicationListener<EnvironmentChangeEvent> {
    private var applicationContext: ApplicationContext? = null
    private val errors: MutableMap<String?, Exception> = ConcurrentHashMap()

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    /**
     * A map of bean name to errors when instantiating the bean.
     *
     * @return The errors accumulated since the latest destroy.
     */
    fun getErrors(): Map<String?, Exception> {
        return errors
    }

    @ManagedOperation
    fun rebind() {
        errors.clear()
        for (name in beans.beanNames) {
            rebind(name)
        }
    }

    @ManagedOperation
    fun rebind(name: String): Boolean {
        if (!beans.beanNames.contains(name)) {
            return false
        }
        if (applicationContext != null) {
            try {
                var bean: Any? = applicationContext!!.getBean(name)
                if (AopUtils.isAopProxy(bean)) {
                    bean = ProxyUtils.getTargetObject(bean)
                }
                if (bean != null) {
                    // see https://github.com/spring-cloud/spring-cloud-commons/issues/571
                    if (neverRefreshable.contains(bean.javaClass.name)) {
                        return false // ignore
                    }
                    applicationContext!!.autowireCapableBeanFactory.destroyBean(bean)
                    applicationContext!!.autowireCapableBeanFactory.initializeBean(bean, name)
                    return true
                }
            } catch (e: RuntimeException) {
                errors[name] = e
                throw e
            } catch (e: Exception) {
                errors[name] = e
                throw IllegalStateException("Cannot rebind to $name", e)
            }
        }
        return false
    }

    @get:ManagedAttribute
    val neverRefreshable: Set<String>
        get() {
            val neverRefresh = applicationContext!!.environment
                    .getProperty("spring.cloud.refresh.never-refreshable",
                            "com.zaxxer.hikari.HikariDataSource")
            return StringUtils.commaDelimitedListToSet(neverRefresh)
        }

    @get:ManagedAttribute
    val beanNames: Set<String?>
        get() = HashSet(beans.beanNames)

    override fun onApplicationEvent(event: EnvironmentChangeEvent) {
        if (applicationContext == event.source || event.keys == event.source) {
            rebind()
        }
    }
}
