package top.bettercode.summer.tools.optimal.entity


/**
 * 原料
 *
 * @author Peter Wu
 */
data class Material(
        // --------------------------------------------
        // --------------------------------------------
        /** 所属大类  */
        var category: String? = null,

        /** 材料名称  */
        var name: String? = null,

        /** 原料形态  */
        var form: String? = null,

        /** 最终使用量  */
        var solutionValue: Double? = null,

        /** 原料价格  */
        var price: Long? = null,

        /**
         * 原料成份: 总养分 氮含量 磷含量 钾含量 氯离子 水分 水溶磷率 水溶磷 硝态氮 硼 锌 锰 铜 铁 钼 镁 硫 钙 有机质（%） 腐植酸 黄腐酸 活性菌 硅 指标23 指标24 指标25
         * 指标26 指标27 指标28 指标29 指标30 指标31 指标32 指标33 指标34 指标35 指标36 指标37 指标38 指标39 指标40 指标41 指标42 指标43 指标44
         * 指标45 指标46 指标47 指标48 指标49 指标50
         */
        var components: Components? = null
)
