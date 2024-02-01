package top.bettercode.summer.tools.recipe.data


/**
 * 原料成份: 总养分 氮含量 磷含量 钾含量 氯离子 水分 水溶磷率 水溶磷 硝态氮 硼 锌 锰 铜 铁 钼 镁 硫 钙 有机质（%） 腐植酸 黄腐酸 活性菌 硅 指标23 指标24 指标25
 * 指标26 指标27 指标28 指标29 指标30 指标31 指标32 指标33 指标34 指标35 指标36 指标37 指标38 指标39 指标40 指标41 指标42 指标43 指标44
 * 指标45 指标46 指标47 指标48 指标49 指标50
 */
class MaterialForm {
    // --------------------------------------------
    /** 所属大类  */
    var category: String? = null

    /** 材料名称  */
    var name: String? = null

    /** 原料形态  */
    var form: String? = null

    // 氮含量
    var nitrogen: Double? = null

    // 磷含量
    var phosphorus: Double? = null

    // 钾含量
    var potassium: Double? = null

    // 氯离子
    var chlorine: Double? = null

    // 水分
    var water: Double? = null

    // 水溶磷率
    var waterSolublePhosphorusRate: Double? = null

    // 水溶磷
    var waterSolublePhosphorus: Double? = null

    // 硝态氮
    var nitrateNitrogen: Double? = null

    // 硼
    var boron: Double? = null

    // 锌
    var zinc: Double? = null

    // 锰
    var manganese: Double? = null

    // 铜
    var copper: Double? = null

    // 铁
    var iron: Double? = null

    // 钼
    var molybdenum: Double? = null

    // 镁
    var magnesium: Double? = null

    // 硫
    var sulfur: Double? = null

    // 钙
    var calcium: Double? = null

    // 有机质（%）
    var organicMatter: Double? = null

    // 腐植酸
    var humicAcid: Double? = null

    // 黄腐酸
    var fulvicAcid: Double? = null

    // 活性菌
    var activeBacteria: Double? = null

    // 硅
    var silicon: Double? = null

    // 指标23-50，类型和名称根据需要修改
    var index23: Double? = null
    var index24: Double? = null
    var index25: Double? = null
    var index26: Double? = null
    var index27: Double? = null
    var index28: Double? = null
    var index29: Double? = null
    var index30: Double? = null
    var index31: Double? = null
    var index32: Double? = null
    var index33: Double? = null
    var index34: Double? = null
    var index35: Double? = null
    var index36: Double? = null
    var index37: Double? = null
    var index38: Double? = null
    var index39: Double? = null
    var index40: Double? = null
    var index41: Double? = null
    var index42: Double? = null
    var index43: Double? = null
    var index44: Double? = null
    var index45: Double? = null
    var index46: Double? = null
    var index47: Double? = null
    var index48: Double? = null
    var index49: Double? = null
    var index50: Double? = null
    val totalNutrient: Double
        // --------------------------------------------
        get() = nitrogen!! + phosphorus!! + potassium!!
}
