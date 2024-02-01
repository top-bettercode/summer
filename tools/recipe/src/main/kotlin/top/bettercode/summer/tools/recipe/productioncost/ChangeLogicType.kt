package top.bettercode.summer.tools.recipe.productioncost

/**
 * 变更逻辑类型
 * @author Peter Wu
 */
enum class ChangeLogicType {
    /**
     * 使用指定原料，且物料水分超过
     */
    WATER_OVER,

    /**
     * 使用指定原料超过
     */
    OVER,

    /**
     * 其他
     */
    OTHER
}