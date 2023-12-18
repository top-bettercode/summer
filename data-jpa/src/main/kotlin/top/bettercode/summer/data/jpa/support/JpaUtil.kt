package top.bettercode.summer.data.jpa.support

import org.hibernate.type.spi.TypeConfiguration
import org.slf4j.LoggerFactory
import org.springframework.util.ClassUtils
import top.bettercode.summer.web.support.ApplicationContextHolder

/**
 * @author Peter Wu
 */

object JpaUtil {

    private val log = LoggerFactory.getLogger(JpaUtil::class.java)
    private val TYPE_CONFIGURATION = TypeConfiguration()

    @Suppress("UNCHECKED_CAST")
    fun <T> convert(source: Any?, targetType: Class<T>?): T? {
        return if (source != null && !targetType!!.isInstance(source)) {
            try {
                TYPE_CONFIGURATION.javaTypeRegistry
                        .getDescriptor<T>(ClassUtils.resolvePrimitiveIfNecessary(targetType))
                        .wrap(source, null)
            } catch (e: Exception) {
                ApplicationContextHolder.conversionService.convert(source, targetType)
            }
        } else {
            source as T?
        }
    }

}
