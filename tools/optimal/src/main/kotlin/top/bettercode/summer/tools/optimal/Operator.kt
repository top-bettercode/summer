package top.bettercode.summer.tools.optimal

/**
 * 操作符
 * @author Peter Wu
 */
enum class Operator(val operator: String) {
    /**
     * 等于
     */
    EQ("="),

    /**
     * 不等于
     */
    NE("≠"),

    /**
     * 大于等于
     */
    GE("≥"),

    /**
     * 大于
     */
    GT(">"),

    /**
     * 小于等于
     */
    LE("≤"),

    /**
     * 小于
     */
    LT("<");

    companion object {
        private val otherOperator: Map<String, String> = mapOf(
            "<=" to "≤",
            ">=" to "≥",
            "!=" to "≠",
        )

        @JvmStatic
        fun validate(operator: String): Boolean {
            for (sense in values()) {
                if (sense.operator == (otherOperator[operator] ?: operator)) {
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun of(operator: String): Operator {
            for (sense in values()) {
                if (sense.operator == (otherOperator[operator] ?: operator)) {
                    return sense
                }
            }
            throw IllegalArgumentException("symbol=$operator not found")
        }
    }

    override fun toString(): String {
        return operator
    }

}
