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
    var applicationContext: ApplicationContext? = null


    @JvmStatic
    fun <T> getBean(s: String, clazz: Class<T>): T? {
        return applicationContext?.getBean(s, clazz)
    }

    @JvmStatic
    fun <T> getBean(clazz: Class<T>): T? {
        return applicationContext?.getBean(clazz)
    }

    @JvmStatic
    fun getMessage(s: String, objects: Array<Any?>?, locale: Locale): String? {
        return applicationContext?.getMessage(s, objects, locale)
    }

    @JvmStatic
    fun getProperty(key: String): String? {
        return applicationContext?.environment?.getProperty(key)
    }

    @JvmStatic
    fun getProperty(key: String, defaultValue: String): String? {
        return applicationContext?.environment?.getProperty(key, defaultValue)
    }

    @JvmStatic
    fun <T> getProperty(key: String, targetType: Class<T>): T? {
        return applicationContext?.environment?.getProperty(key, targetType)
    }

    @JvmStatic
    fun <T> getProperty(key: String, targetType: Class<T>, defaultValue: T): T? {
        return applicationContext?.environment?.getProperty(key, targetType, defaultValue!!)
    }

    @JvmStatic
    val activeProfiles: Array<String>
        get() = applicationContext?.environment?.activeProfiles ?: emptyArray()

    @JvmStatic
    val isTest: Boolean
        get() = activeProfiles.any { it.contains("test") }

    @JvmStatic
    val isDev: Boolean
        get() = activeProfiles.any { it.contains("dev") }

    @JvmStatic
    val conversionService: ConversionService
        get() = applicationContext?.getBean(ConversionService::class.java)
                ?: DefaultConversionService()

    @JvmStatic
    val requestAttributes: Optional<ServletRequestAttributes>
        get() {
            val requestAttributes = RequestContextHolder
                    .getRequestAttributes() as ServletRequestAttributes
            return Optional.ofNullable(requestAttributes)
        }

}
