package top.bettercode.summer.tools.optimal.entity

import java.math.BigDecimal

/**
 * 限制
 *
 * @author Peter Wu
 */
class Limit {
    // --------------------------------------------
    // --------------------------------------------
    /** 最小限制值  */
    var min: BigDecimal? = null

    /** 最大限制值  */
    var max: BigDecimal? = null

    /** 限用原料名称  */
    var materials: List<String>? = null

    val value: BigDecimal?
        get() = min

    // --------------------------------------------
    constructor()
    constructor(value: BigDecimal?) {
        this.min = value
        this.max = value
    }

    constructor(min: BigDecimal?, max: BigDecimal?) {
        this.min = min
        this.max = max
    }

    constructor(materials: List<String>) {
        this.materials = materials
    }
}
