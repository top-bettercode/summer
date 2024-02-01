package top.bettercode.summer.tools.recipe.productioncost

/**
 * 制造费用项标识
 * @author Peter Wu
 */
enum class DictType {
    /**
     * 能耗费用
     */
    ENERGY,

    /**
     * 人工
     */
    STAFF,

    /**
     * 折旧费
     */
    DEPRECIATION,

    /**
     * 其他费用
     */
    OTHER
}