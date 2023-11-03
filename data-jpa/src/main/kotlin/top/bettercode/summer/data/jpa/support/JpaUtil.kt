package top.bettercode.summer.data.jpa.support

import top.bettercode.summer.web.support.ApplicationContextHolder

/**
 * @author Peter Wu
 */
object JpaUtil {

    @Suppress("UNCHECKED_CAST")
    fun <T> convert(source: Any?, targetType: Class<T>?): T? {
        return if (source != null && !targetType!!.isInstance(source)) {
            if ((targetType == Boolean::class.javaObjectType || targetType == Boolean::class.java) && source is Number) {
                (source.toInt() > 0) as T?
            } else
                ApplicationContextHolder.conversionService.convert(source, targetType)
        } else {
            source as T?
        }
    }
}
