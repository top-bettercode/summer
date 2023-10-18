package top.bettercode.summer.tools.optimal.result

import top.bettercode.summer.tools.optimal.entity.Components
import top.bettercode.summer.tools.optimal.entity.Material
import java.math.BigDecimal

/**
 * 配方
 *
 * @author Peter Wu
 */
class Recipe {
    // --------------------------------------------
    /** 配方序号  */
    var index = 0

    /** 选用的原料  */
    var materials: MutableList<Material> = ArrayList()

    /** 硫酸一般用量  */
    var vitriolNormal: BigDecimal? = null

    /** 硫酸过量用量  */
    var vitriolExcess: BigDecimal? = null

    /** 需要烘干的水分含量  */
    var dryWater: BigDecimal? = null

    /** 目标值  */
    var objectiveValue: BigDecimal? = null

    /** 配方成本  */
    var cost: BigDecimal? = null

    /** 成品成份,总养分 氮含量 磷含量 水溶磷率 钾含量 氯离子 水分 硼 锌 成份限用原料  */
    var componentRecipe: Components? = null

    /** 原料包含碳铵  */
    var isHascliquidAmmonia = false

    /** 最小耗液氨用量  */
    var minLiquidAmmoniaWeight: BigDecimal? = null

    /** 最大耗液氨用量  */
    var maxLiquidAmmoniaWeight: BigDecimal? = null

    /** 硫酸用量  */
    var vitriolWeight: BigDecimal? = null

    /** 液氨用量  */
    var liquidAmmoniaWeight: BigDecimal? = null

    // --------------------------------------------
    fun addMaterial(material: Material) {
        materials.add(material)
    }

}
