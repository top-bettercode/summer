package top.bettercode.lang.util

import org.springframework.util.Assert

/**
 * @author Peter Wu
 */
object BeanUtil {

    @JvmStatic
    fun copyPropertiesNotNull(exist: Any, newEntity: Any) {
        copyPropertiesNotNullOrEmpty(exist, newEntity)
    }

    @JvmStatic
    fun copyPropertiesNotEmpty(exist: Any, newEntity: Any) {
        copyPropertiesNotNullOrEmpty(exist, newEntity, true)
    }

    private fun copyPropertiesNotNullOrEmpty(
        exist: Any,
        newEntity: Any,
        ignoreEmpty: Boolean = false
    ) {
        Assert.notNull(exist, "exist must not be null")
        Assert.notNull(newEntity, "newEntity must not be null")
        val existWrapper = DirectFieldAccessFallbackBeanWrapper(
            exist
        )
        val newWrapper = DirectFieldAccessFallbackBeanWrapper(
            newEntity
        )
        val targetPds = newWrapper.propertyDescriptors
        for (targetPd in targetPds) {
            val propertyName = targetPd.name
            if ("class" == propertyName) {
                continue
            }
            val propertyValue = newWrapper.getPropertyValue(propertyName)
            if (propertyValue != null && (!ignoreEmpty || "" != propertyValue)) {
                continue
            }
            newWrapper.setPropertyValue(propertyName, existWrapper.getPropertyValue(propertyName))
        }
    }

}