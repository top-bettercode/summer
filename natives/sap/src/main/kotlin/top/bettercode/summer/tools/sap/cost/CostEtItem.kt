package top.bettercode.summer.tools.sap.cost

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapField

class CostEtItem {
    /**
     * 成本中心
     */
    @SapField("KOSTL")
    var kostl: String? = null

    /**
     * 公司代码
     */
    @SapField("BUKRS")
    var bukrs: String? = null

    /**
     * 长文本
     */
    @SapField("LTEXT")
    var ltext: String? = null

    /**
     * 功能范围
     */
    @SapField("FUNC_AREA")
    var funcArea: String? = null

    /**
     * 功能范围的名称
     */
    @SapField("FKBTX")
    var fkbtx: String? = null

    override fun toString(): String {
        return json(this)
    }
}