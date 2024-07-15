package top.bettercode.summer.data.jpa.support

import org.hibernate.type.spi.TypeConfiguration
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.data.domain.Pageable
import org.springframework.util.ClassUtils
import top.bettercode.summer.tools.lang.log.SqlAppender
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.cost
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.limit
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.offset
import top.bettercode.summer.web.support.ApplicationContextHolder

/**
 * @author Peter Wu
 */

object JpaUtil {

    private val sqlLog = LoggerFactory.getLogger("top.bettercode.summer.SQL")
    private val log = LoggerFactory.getLogger(JpaUtil::class.java)
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

    fun <M> mdcId(id: String, pageable: Pageable? = null, run: () -> M): M {
        val put = if (MDC.get(SqlAppender.MDC_SQL_ID) == null) {
            MDC.put(SqlAppender.MDC_SQL_ID, id)
            true
        } else {
            false
        }
        try {
            return if (put) {
                val s = System.currentTimeMillis()
                try {
                    if (pageable != null && pageable.isPaged) {
                        val pageSize = pageable.pageSize
                        val offset = pageable.offset
                        sqlLog.offset(offset)
                        sqlLog.limit(pageSize)
                    }
                    run()
                } catch (e: Exception) {
                    MDC.put(SqlAppender.MDC_SQL_ERROR, e.stackTraceToString())
                    throw e
                } finally {
                    val duration = System.currentTimeMillis() - s
                    sqlLog.cost(duration)
                }
            } else {
                run()
            }
        } finally {
            MDC.remove(SqlAppender.MDC_SQL_ERROR)
            if (put) {
                MDC.remove(SqlAppender.MDC_SQL_ID)
            }
        }
    }

}
