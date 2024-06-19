package top.bettercode.summer.tools.recipe.indicator

/**
 * %,公斤/吨,亿
 * @author Peter Wu
 */
enum class IndicatorUnit(val unit: String) {
    /**
     * %
     */
    PERCENTAGE("%"),

    /**
     * 公斤/吨
     */
    KG_TON("公斤/吨"),

    /**
     * 亿/公斤
     */
    BILLION("亿");

    companion object{
        fun enumOf(unit: String): IndicatorUnit {
            return values().first { it.unit == unit }
        }
    }

    fun eq(unit: String?): Boolean {
        return this.unit == unit
    }
}