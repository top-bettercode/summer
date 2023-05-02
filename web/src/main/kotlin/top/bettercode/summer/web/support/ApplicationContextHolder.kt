package top.bettercode.summer.web.support

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.NoSuchMessageException
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

/**
 * @author Peter Wu
 */
class ApplicationContextHolder : ApplicationContextAware {
    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        Companion.applicationContext = applicationContext
    }

    companion object {
        @JvmStatic
        var applicationContext: ApplicationContext? = null
            private set

        @Throws(BeansException::class)
        fun <T> getBean(s: String, aClass: Class<T>): T? {
            return if (applicationContext == null) null else applicationContext!!.getBean(s, aClass)
        }

        @Throws(BeansException::class)
        fun <T> getBean(aClass: Class<T>): T? {
            return if (applicationContext == null) null else applicationContext!!.getBean(aClass)
        }

        @Throws(NoSuchMessageException::class)
        fun getMessage(s: String, objects: Array<Any?>?, locale: Locale): String? {
            return if (applicationContext == null) null else applicationContext!!.getMessage(s, objects, locale)
        }

        @JvmStatic
        fun getProperty(key: String): String? {
            return if (applicationContext == null) null else applicationContext!!.environment.getProperty(key)
        }

        fun getProperty(key: String, defaultValue: String): String? {
            return if (applicationContext == null) null else applicationContext!!.environment.getProperty(key, defaultValue)
        }

        fun <T> getProperty(key: String, targetType: Class<T>): T? {
            return if (applicationContext == null) null else applicationContext!!.environment.getProperty(key, targetType)
        }

        fun <T> getProperty(key: String, targetType: Class<T>, defaultValue: T): T? {
            return if (applicationContext == null) null else applicationContext!!.environment.getProperty(key, targetType, defaultValue)
        }

        @JvmStatic
        val conversionService: ConversionService
            get() = if (applicationContext == null) DefaultConversionService() else applicationContext!!.getBean(ConversionService::class.java)
        val requestAttributes: Optional<ServletRequestAttributes>
            get() {
                val requestAttributes = RequestContextHolder
                        .getRequestAttributes() as ServletRequestAttributes
                return Optional.ofNullable(requestAttributes)
            }
    }
}
