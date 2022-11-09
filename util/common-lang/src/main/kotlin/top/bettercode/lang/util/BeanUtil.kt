package top.bettercode.lang.util

import org.springframework.util.Assert

/**
 * @author Peter Wu
 */
object BeanUtil {

    @JvmStatic
    fun Any.nullPropertySetFrom(exist: Any) {
        this.nullOrEmptyPropertySetFrom(exist, false)
    }

    @JvmStatic
    fun Any.nullOrEmptyPropertySetFrom(
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