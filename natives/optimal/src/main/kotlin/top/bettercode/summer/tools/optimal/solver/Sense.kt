package top.bettercode.summer.tools.optimal.solver

/**
 * 操作符
 * @author Peter Wu
 */
enum class Sense(val symbol: String) {
    /**
     * 等于
     */
    EQ("="),

    /**
     * 不等于
     */
    NE("!="),

    /**
     * 大于等于
     */
    GE(">="),

    /**
     * 大于
     */
    GT(">"),

    /**
     * 小于等于
     */
    LE("<="),

    /**
     * 小于
     */
    LT("<");


    companion object {
        @JvmStatic
        fun validate(symbol: String): Boolean {
            for (sense in values()) {
                if (sense.symbol == symbol) {
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun symbolOf(symbol: String): Sense {
            for (sense in values()) {
                if (sense.symbol == symbol) {
                    return sense
                }
            }
            throw IllegalArgumentException("symbol=$symbol not found")
        }
    }

    override fun toString(): String {
        return symbol
    }

}
