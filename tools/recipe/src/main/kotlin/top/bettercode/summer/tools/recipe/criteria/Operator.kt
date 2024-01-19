package top.bettercode.summer.tools.recipe.criteria

/**
 * 操作符
 * @author Peter Wu
 */
enum class Operator(val symbol: String) {
    /**
     * 等于
     */
    EQUAL("="),

    /**
     * 大于
     */
    GREATER(">"),

    /**
     * 小于
     */
    LESS("<"),

    /**
     * 大于等于
     */
    GREATER_EQUAL(">="),

    /**
     * 小于等于
     */
    LESS_EQUAL("<=");

    override fun toString(): String {
        return symbol
    }

}
