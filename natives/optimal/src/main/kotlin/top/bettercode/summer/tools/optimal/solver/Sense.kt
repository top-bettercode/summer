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


    override fun toString(): String {
        return symbol
    }

}
