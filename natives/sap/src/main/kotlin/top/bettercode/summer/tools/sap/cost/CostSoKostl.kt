package top.bettercode.summer.tools.sap.cost

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapField

class CostSoKostl {
    /**
     * 借方/贷方符号 (+/-)
     */
    @SapField("SIGN")
    var sign: String? = null

    /**
     * 范围表选项
     */
    @SapField("OPTION")
    var option: String? = null

    /**
     * 成本中心
     */
    @SapField("LOW")
    var low: String? = null

    /**
     * 成本中心
     */
    @SapField("HIGH")
    var high: String? = null

    override fun toString(): String {
        return json(this)
    }
}