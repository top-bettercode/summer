package top.bettercode.summer.env

import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.context.properties.ConfigurationPropertiesBean
import org.springframework.context.*
import org.springframework.stereotype.Component

/**
 * Collects references to `@ConfigurationProperties` beans in the context and its parent.
 *
 * @author Dave Syer
 */
@Component
class ConfigurationPropertiesBeans : BeanPostProcessor, ApplicationContextAware {
    private val beans: MutableMap<String, ConfigurationPropertiesBean> = HashMap()
    private var applicationContext: ApplicationContext? = null
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        if (applicationContext.autowireCapableBeanFactory is ConfigurableListableBeanFactory) {
            applicationContext.autowireCapableBeanFactory
        }
        if (applicationContext.parent != null && applicationContext.parent
                ?.autowireCapableBeanFactory is ConfigurableListableBeanFactory) {
            val listable = applicationContext.parent
                    ?.autowireCapableBeanFactory as ConfigurableListableBeanFactory
            val names = listable.getBeanNamesForType(ConfigurationPropertiesBeans::class.java)
            if (names.size == 1) {
                val parent = listable.getBean(
                        names[0]) as ConfigurationPropertiesBeans
                beans.putAll(parent.beans)
            }
        }
    }

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val propertiesBean = ConfigurationPropertiesBean.get(
                applicationContext, bean,
                beanName)
        if (propertiesBean != null) {
            beans[beanName] = propertiesBean
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        return bean
    }

    val beanNames: Set<String>
        get() = HashSet(beans.keys)
}
