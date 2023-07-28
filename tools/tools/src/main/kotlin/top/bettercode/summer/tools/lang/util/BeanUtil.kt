package top.bettercode.summer.tools.lang.util

import org.springframework.util.Assert

/**
 * @author Peter Wu
 */
object BeanUtil {

    @JvmStatic
    fun Any.nullFrom(exist: Any) {
        this.nullOrEmptyFrom(exist, false)
    }

    @JvmStatic
    fun Any.nullOrEmptyFrom(
            exist: Any,
            setEmptyProperty: Boolean = true
    ) {
        Assert.notNull(exist, "exist must not be null")
        Assert.notNull(this, "newEntity must not be null")
        val existWrapper = DirectFieldAccessFallbackBeanWrapper(exist)
        val thisWrapper = DirectFieldAccessFallbackBeanWrapper(this)
        val targetPds = thisWrapper.propertyDescriptors
        for (targetPd in targetPds) {
            val propertyName = targetPd.name
            if ("class" == propertyName) {
                continue
            }
            val propertyValue = thisWrapper.getPropertyValue(propertyName)
            if (propertyValue != null && (!setEmptyProperty || "" != propertyValue)) {
                continue
            }
            thisWrapper.setPropertyValue(propertyName, existWrapper.getPropertyValue(propertyName))
        }
    }

}