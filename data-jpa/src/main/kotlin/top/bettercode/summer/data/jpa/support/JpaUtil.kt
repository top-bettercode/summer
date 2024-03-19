package top.bettercode.summer.data.jpa.support

import org.hibernate.type.spi.TypeConfiguration
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.util.ClassUtils
import top.bettercode.summer.tools.lang.log.AlarmMarker
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
                val duration = System.currentTimeMillis() - s
                val timeoutAlarmSeconds = (ApplicationContextHolder.getProperty("summer.data.jpa.timeout-alarm-seconds", Int::class.java)
                        ?: -1)
                if (timeoutAlarmSeconds > 0 && duration > timeoutAlarmSeconds * 1000) {
                    if (ApplicationContextHolder.isTest || ApplicationContextHolder.isDev) {
                        val initialComment = "$id：执行速度慢(${duration / 1000}秒)"
                        log.warn(AlarmMarker(initialComment, true), initialComment + "cost:{}ms", duration)
                    } else {
                        log.warn("cost:{}ms", duration)
                    }
                } else {
                    if (duration > 2 * 1000) {
                        log.warn("cost:{}ms", duration)
                    } else {
                        log.debug("cost:{}ms", duration)
                    }
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
