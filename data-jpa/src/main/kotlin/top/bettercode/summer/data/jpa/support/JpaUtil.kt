package top.bettercode.summer.data.jpa.support

import org.hibernate.type.spi.TypeConfiguration
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.MarkerFactory
import org.springframework.util.ClassUtils
import top.bettercode.summer.logging.RequestLoggingFilter
import top.bettercode.summer.web.form.IFormkeyService.Companion.log
import top.bettercode.summer.web.support.ApplicationContextHolder

/**
 * @author Peter Wu
 */

object JpaUtil {

    private val sqlLog = LoggerFactory.getLogger("org.hibernate.SQL")
    private val TYPE_CONFIGURATION = TypeConfiguration()

    @Suppress("UNCHECKED_CAST")
    fun <T> convert(source: Any?, targetType: Class<T>?): T? {
        return if (source != null && !targetType!!.isInstance(source)) {
            try {
                TYPE_CONFIGURATION.javaTypeDescriptorRegistry
                        .getDescriptor(ClassUtils.resolvePrimitiveIfNecessary(targetType))
                        .wrap(source, null) as T?
            } catch (e: Exception) {
                ApplicationContextHolder.conversionService.convert(source, targetType)
            }
        } else {
            source as T?
        }
    }

    fun <M> mdcId(id: String, run: () -> M): M {
        val put = if (MDC.get("id") == null) {
            MDC.put("id", id)
            true
        } else {
            false
        }
        try {
            return if (put) {
                val s = System.currentTimeMillis()
                val result = run()
                val d = System.currentTimeMillis() - s
                val timeoutAlarmSeconds = (ApplicationContextHolder.getProperty("summer.data.jpa.timeout-alarm-seconds", Int::class.java)
                        ?: 2) * 1000
                if (d > timeoutAlarmSeconds) {
                    if (ApplicationContextHolder.isTest || ApplicationContextHolder.isDev) {
                        log.warn(MarkerFactory.getMarker(RequestLoggingFilter.ALARM_LOG_MARKER), "cost:{}ms", d)
                    } else {
                        log.warn("cost:{}ms", d)
                    }
                } else {
                    log.debug("cost:{}ms", d)
                }
                result
            } else {
                run()
            }
        } finally {
            if (put) {
                MDC.remove("id")
            }
        }
    }

}
