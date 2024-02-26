package top.bettercode.summer.tools.recipe.indicator

/**
 *
 * @author Peter Wu
 */
enum class RecipeIndicatorType {
    /**
     * 常规
     */
    GENERAL,

    /**
     * 养分
     */
    NUTRIENT,

    /**
     * 产品水分
     */
    PRODUCT_WATER,

    /**
     * 物料水分
     */
    WATER,

    /**
     *和另一指标的比率
     */
    RATE_TO_OTHER,

    /**
     * 特殊指标
     */
    SPECIAL
}
