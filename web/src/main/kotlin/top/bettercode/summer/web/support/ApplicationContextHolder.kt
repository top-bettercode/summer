package top.bettercode.summer.web.support

import org.springframework.context.ApplicationContext
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

/**
 * @author Peter Wu
 */
object ApplicationContextHolder {
    @JvmStatic
    lateinit var applicationContext: ApplicationContext

    @JvmStatic
    val isInitialized: Boolean
        get() = ::applicationContext.isInitialized

    @JvmStatic
    fun <T> getBean(s: String, aClass: Class<T>): T? {
        return applicationContext.getBean(s, aClass)
    }

    @JvmStatic
    fun <T> getBean(aClass: Class<T>): T? {
        return applicationContext.getBean(aClass)
    }

    @JvmStatic
    fun getMessage(s: String, objects: Array<Any?>?, locale: Locale): String? {
        return applicationContext.getMessage(s, objects, locale)
    }

    @JvmStatic
    fun getProperty(key: String): String? {
        return applicationContext.environment.getProperty(key)
    }

    @JvmStatic
    fun getProperty(key: String, defaultValue: String): String? {
        return applicationContext.environment.getProperty(key, defaultValue)
    }

    @JvmStatic
    fun <T> getProperty(key: String, targetType: Class<T>): T? {
        return applicationContext.environment.getProperty(key, targetType)
    }

    @JvmStatic
    fun <T> getProperty(key: String, targetType: Class<T>, defaultValue: T): T? {
        return applicationContext.environment.getProperty(key, targetType, defaultValue!!)
    }

    @JvmStatic
    val conversionService: ConversionService
        get() = if (isInitialized) applicationContext.getBean(ConversionService::class.java) else DefaultConversionService()

    @JvmStatic
    val requestAttributes: Optional<ServletRequestAttributes>
        get() {
            val requestAttributes = RequestContextHolder
                    .getRequestAttributes() as ServletRequestAttributes
            return Optional.ofNullable(requestAttributes)
        }
}
