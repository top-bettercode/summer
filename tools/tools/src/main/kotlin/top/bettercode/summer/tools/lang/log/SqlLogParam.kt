package top.bettercode.summer.tools.lang.log

import top.bettercode.summer.tools.lang.util.JavaType

/**
 *
 * @author Peter Wu
 */
data class SqlLogParam(val index: Int, val type: JavaType?, val value: String?) {
    override fun toString(): String {
        val value = if (value == null || "null" == value)
            null
        else
            when (type) {
                JavaType.stringInstance, JavaType.dateInstance, JavaType("java.time.LocalDate"), JavaType(
                    "java.time.LocalDateTime"
                ) -> "'${this.value}'"

                else -> this.value
            }
        return "#$value"
    }
}