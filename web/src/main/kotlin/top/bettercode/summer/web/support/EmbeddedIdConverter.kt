package top.bettercode.summer.web.support

import org.springframework.beans.BeanUtils
import org.springframework.util.ReflectionUtils
import java.beans.PropertyDescriptor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author Peter Wu
 */
object EmbeddedIdConverter {
    private val cache: ConcurrentMap<Class<*>, List<PropertyDescriptor>> = ConcurrentHashMap()
    const val DELIMITER = ","

    @JvmStatic
    fun <T : Any> toString(embeddedId: T): String {
        return toString(embeddedId, DELIMITER)
    }

    @JvmStatic
    fun <T : Any> toString(embeddedId: T, delimiter: String): String {
        val clazz: Class<*> = embeddedId.javaClass
        return getPropertyDescriptors(clazz).joinToString(delimiter) { o: PropertyDescriptor ->
            val value = ReflectionUtils.invokeMethod(o.readMethod, embeddedId)
            ApplicationContextHolder.conversionService.convert(value, String::class.java) ?: ""
        }
    }

    private fun getPropertyDescriptors(clazz: Class<*>): List<PropertyDescriptor> {
        return cache.computeIfAbsent(clazz) { c: Class<*> ->
            BeanUtils.getPropertyDescriptors(c)
                .filter { o: PropertyDescriptor -> "class" != o.name && o.readMethod != null && o.writeMethod != null }
                .sortedBy { it.name }
        }
    }

    @JvmStatic
    fun <T> toEmbeddedId(src: String?, type: Class<T>): T? {
        return toEmbeddedId(src, DELIMITER, type)
    }

    @JvmStatic
    fun <T> toEmbeddedId(src: String?, delimiter: String, type: Class<T>): T? {
        if (src.isNullOrBlank()) {
            return null
        }
        val values = src.split(delimiter)
        val result = BeanUtils.instantiateClass(type)
        val descriptors = getPropertyDescriptors(type)
        for (i in descriptors.indices) {
            val descriptor = descriptors[i]
            val value = values[i]
            ReflectionUtils.invokeMethod(
                descriptor.writeMethod, result,
                ApplicationContextHolder.conversionService.convert(value, descriptor.propertyType)
            )
        }
        return result
    }
}
