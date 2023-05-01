package top.bettercode.summer.web.support

import org.springframework.beans.BeanUtils
import org.springframework.util.ReflectionUtils
import java.beans.PropertyDescriptor
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Function
import java.util.regex.Pattern
import java.util.stream.Collectors

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
    fun <T : Any> toString(embeddedId: T, delimiter: String?): String {
        val clazz: Class<*> = embeddedId.javaClass
        return getPropertyDescriptors(clazz).stream()
                .map<String>(Function<PropertyDescriptor, String> { o: PropertyDescriptor ->
                    ApplicationContextHolder.conversionService.convert<String>(
                            ReflectionUtils.invokeMethod(o.readMethod, embeddedId), String::class.java)
                })
                .collect(Collectors.joining(delimiter))
    }

    private fun getPropertyDescriptors(clazz: Class<*>): List<PropertyDescriptor> {
        return cache.computeIfAbsent(clazz) { c: Class<*> ->
            Arrays.stream(BeanUtils.getPropertyDescriptors(c))
                    .filter { o: PropertyDescriptor -> "class" != o.name && o.readMethod != null && o.writeMethod != null }.sorted(
                            Comparator.comparing { obj: PropertyDescriptor -> obj.name }).collect(Collectors.toList())
        }
    }

    @JvmStatic
    fun <T> toEmbeddedId(src: String?, type: Class<T>): T {
        return toEmbeddedId(src, DELIMITER, type)
    }

    @JvmStatic
    fun <T> toEmbeddedId(src: String?, delimiter: String, type: Class<T>): T {
        val values = Pattern.compile(delimiter).split(src)
        val result = BeanUtils.instantiateClass(type)
        val descriptors = getPropertyDescriptors(type)
        for (i in descriptors.indices) {
            val descriptor = descriptors[i]
            ReflectionUtils.invokeMethod(
                    descriptor.writeMethod, result,
                    ApplicationContextHolder.conversionService
                            .convert(values[i], descriptor.propertyType))
        }
        return result
    }
}
