package top.bettercode.summer.tools.recipe.criteria

/**
 * 操作符
 * @author Peter Wu
 */
enum class Operator(val symbol: String) {
    /**
     * 等于
     */
    EQ("="),
    /**
     * 不等于
     */
    NE("!="),
    /**
     * 大于
     */
    GT(">"),

    /**
     * 小于
     */
    LT("<"),

    /**
     * 大于等于
     */
    GE(">="),

    /**
     * 小于等于
     */
    LE("<=");

    override fun toString(): String {
        return symbol
    }

}
