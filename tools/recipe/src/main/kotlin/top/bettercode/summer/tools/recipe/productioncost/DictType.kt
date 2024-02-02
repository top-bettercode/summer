package top.bettercode.summer.tools.recipe.productioncost

/**
 * 制造费用项标识
 * @author Peter Wu
 */
enum class DictType( val remark: String) {
    /**
     * 能耗费用
     */
    ENERGY("能耗费用"),

    /**
     * 人工
     */
    STAFF("人工费"),

    /**
     * 折旧费
     */
    DEPRECIATION("折旧费"),

    /**
     * 其他费用
     */
    OTHER("其他费用");

}