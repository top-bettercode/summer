package top.bettercode.summer.tools.optimal.form

import java.math.BigDecimal

/**
 * 原料成份: 总养分 氮含量 磷含量 钾含量 氯离子 水分 水溶磷率 水溶磷 硝态氮 硼 锌 锰 铜 铁 钼 镁 硫 钙 有机质（%） 腐植酸 黄腐酸 活性菌 硅 指标23 指标24 指标25
 * 指标26 指标27 指标28 指标29 指标30 指标31 指标32 指标33 指标34 指标35 指标36 指标37 指标38 指标39 指标40 指标41 指标42 指标43 指标44
 * 指标45 指标46 指标47 指标48 指标49 指标50
 */
class ComponentsForm {
    // --------------------------------------------
    /** 所属大类  */
    var category: String? = null

    /** 材料名称  */
    var name: String? = null

    /** 原料形态  */
    var form: String? = null

    // 氮含量
    var nitrogen: BigDecimal? = null

    // 磷含量
    var phosphorus: BigDecimal? = null

    // 钾含量
    var potassium: BigDecimal? = null

    // 氯离子
    var chlorine: BigDecimal? = null

    // 水分
    var water: BigDecimal? = null

    // 水溶磷率
    var waterSolublePhosphorusRate: BigDecimal? = null

    // 水溶磷
    var waterSolublePhosphorus: BigDecimal? = null

    // 硝态氮
    var nitrateNitrogen: BigDecimal? = null

    // 硼
    var boron: BigDecimal? = null

    // 锌
    var zinc: BigDecimal? = null

    // 锰
    var manganese: BigDecimal? = null

    // 铜
    var copper: BigDecimal? = null

    // 铁
    var iron: BigDecimal? = null

    // 钼
    var molybdenum: BigDecimal? = null

    // 镁
    var magnesium: BigDecimal? = null

    // 硫
    var sulfur: BigDecimal? = null

    // 钙
    var calcium: BigDecimal? = null

    // 有机质（%）
    var organicMatter: BigDecimal? = null

    // 腐植酸
    var humicAcid: BigDecimal? = null

    // 黄腐酸
    var fulvicAcid: BigDecimal? = null

    // 活性菌
    var activeBacteria: BigDecimal? = null

    // 硅
    var silicon: BigDecimal? = null

    // 指标23-50，类型和名称根据需要修改
    var index23: BigDecimal? = null
    var index24: BigDecimal? = null
    var index25: BigDecimal? = null
    var index26: BigDecimal? = null
    var index27: BigDecimal? = null
    var index28: BigDecimal? = null
    var index29: BigDecimal? = null
    var index30: BigDecimal? = null
    var index31: BigDecimal? = null
    var index32: BigDecimal? = null
    var index33: BigDecimal? = null
    var index34: BigDecimal? = null
    var index35: BigDecimal? = null
    var index36: BigDecimal? = null
    var index37: BigDecimal? = null
    var index38: BigDecimal? = null
    var index39: BigDecimal? = null
    var index40: BigDecimal? = null
    var index41: BigDecimal? = null
    var index42: BigDecimal? = null
    var index43: BigDecimal? = null
    var index44: BigDecimal? = null
    var index45: BigDecimal? = null
    var index46: BigDecimal? = null
    var index47: BigDecimal? = null
    var index48: BigDecimal? = null
    var index49: BigDecimal? = null
    var index50: BigDecimal? = null
    val totalNutrient: BigDecimal
        // --------------------------------------------
        get() = nitrogen!!.add(phosphorus).add(potassium)
}
