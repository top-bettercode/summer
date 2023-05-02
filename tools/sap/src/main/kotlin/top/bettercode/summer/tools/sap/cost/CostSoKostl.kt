package top.bettercode.summer.tools.sap.cost

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapField

class CostSoKostl {
    /**
     * @return 借方/贷方符号 (+/-)
     */
    /**
     * 借方/贷方符号 (+/-)
     */
    @SapField("SIGN")
    var sign: String? = null
        private set
    /**
     * @return 范围表选项
     */
    /**
     * 范围表选项
     */
    @SapField("OPTION")
    var option: String? = null
        private set
    /**
     * @return 成本中心
     */
    /**
     * 成本中心
     */
    @SapField("LOW")
    var low: String? = null
        private set
    /**
     * @return 成本中心
     */
    /**
     * 成本中心
     */
    @SapField("HIGH")
    var high: String? = null
        private set

    /**
     * 设置借方/贷方符号 (+/-)
     *
     * @param sign 借方/贷方符号 (+/-)
     * @return KOSTL的范围
     */
    fun setSign(sign: String?): CostSoKostl {
        this.sign = sign
        return this
    }

    /**
     * 设置范围表选项
     *
     * @param option 范围表选项
     * @return KOSTL的范围
     */
    fun setOption(option: String?): CostSoKostl {
        this.option = option
        return this
    }

    /**
     * 设置成本中心
     *
     * @param low 成本中心
     * @return KOSTL的范围
     */
    fun setLow(low: String?): CostSoKostl {
        this.low = low
        return this
    }

    /**
     * 设置成本中心
     *
     * @param high 成本中心
     * @return KOSTL的范围
     */
    fun setHigh(high: String?): CostSoKostl {
        this.high = high
        return this
    }

    override fun toString(): String {
        return json(this)
    }
}