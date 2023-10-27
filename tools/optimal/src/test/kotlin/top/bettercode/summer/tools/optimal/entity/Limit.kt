package top.bettercode.summer.tools.optimal.entity

import java.math.BigDecimal

/**
 * 限制
 *
 * @author Peter Wu
 */
data class Limit(
        // --------------------------------------------
        // --------------------------------------------
        /** 最小限制值  */
        var min: Double? = null,

        /** 最大限制值  */
        var max: Double? = null,

        /** 限用原料名称  */
        var materials: List<String>? = null
) {
    val value: Double?
        get() = min

    // --------------------------------------------
    constructor(value: Double?) : this(min = value, max = value)
    constructor(min: BigDecimal?, max: BigDecimal?) : this(min = min?.toDouble(), max = max?.toDouble())

    constructor(materials: List<String>) : this(min = null, max = null, materials = materials) {
        this.materials = materials
    }
}
