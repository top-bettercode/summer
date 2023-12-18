package top.bettercode.summer.tools.optimal.entity

/**
 * 原料比率
 *
 * @author Peter Wu
 */
class MaterialRatio {
    /** 过量原始原料对应使用原料用量比率  */
    var originExcess: Limit? = null

    /** 对应使用原料一般用量比率  */
    var normal: Limit? = null

    /** 对应使用原料过量用量比率  */
    var excess: Limit? = null
}
