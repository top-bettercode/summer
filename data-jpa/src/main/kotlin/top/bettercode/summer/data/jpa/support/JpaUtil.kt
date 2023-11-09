@file:Suppress("DEPRECATION")

package top.bettercode.summer.data.jpa.support

import org.hibernate.type.descriptor.java.JavaTypeDescriptor
import org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry
import org.springframework.util.ClassUtils
import top.bettercode.summer.web.support.ApplicationContextHolder

/**
 * @author Peter Wu
 */

object JpaUtil {

    private val TYPE_DESCRIPTOR_REGISTRY = JavaTypeDescriptorRegistry.INSTANCE

    @Suppress("UNCHECKED_CAST")
    fun <T> convert(source: Any?, targetType: Class<T>?): T? {
        return if (source != null && !targetType!!.isInstance(source)) {
            try {
                val descriptor = TYPE_DESCRIPTOR_REGISTRY.getDescriptor(
                        ClassUtils.resolvePrimitiveIfNecessary(targetType)) as JavaTypeDescriptor<T>
                descriptor.wrap(source, null)
            } catch (e: Exception) {
                ApplicationContextHolder.conversionService.convert(source, targetType)
            }
        } else {
            source as T?
        }
    }
}
