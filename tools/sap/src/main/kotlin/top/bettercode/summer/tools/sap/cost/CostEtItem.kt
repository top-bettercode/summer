package top.bettercode.summer.tools.sap.cost

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapField

class CostEtItem {
    /**
     * @return 成本中心
     */
    /**
     * 成本中心
     */
    @SapField("KOSTL")
    var kostl: String? = null
        private set
    /**
     * @return 公司代码
     */
    /**
     * 公司代码
     */
    @SapField("BUKRS")
    var bukrs: String? = null
        private set
    /**
     * @return 长文本
     */
    /**
     * 长文本
     */
    @SapField("LTEXT")
    var ltext: String? = null
        private set
    /**
     * @return 功能范围
     */
    /**
     * 功能范围
     */
    @SapField("FUNC_AREA")
    var funcArea: String? = null
        private set
    /**
     * @return 功能范围的名称
     */
    /**
     * 功能范围的名称
     */
    @SapField("FKBTX")
    var fkbtx: String? = null
        private set

    /**
     * 设置成本中心
     *
     * @param kostl 成本中心
     * @return 成本中心
     */
    fun setKostl(kostl: String?): CostEtItem {
        this.kostl = kostl
        return this
    }

    /**
     * 设置公司代码
     *
     * @param bukrs 公司代码
     * @return 成本中心
     */
    fun setBukrs(bukrs: String?): CostEtItem {
        this.bukrs = bukrs
        return this
    }

    /**
     * 设置长文本
     *
     * @param ltext 长文本
     * @return 成本中心
     */
    fun setLtext(ltext: String?): CostEtItem {
        this.ltext = ltext
        return this
    }

    /**
     * 设置功能范围
     *
     * @param funcArea 功能范围
     * @return 成本中心
     */
    fun setFuncArea(funcArea: String?): CostEtItem {
        this.funcArea = funcArea
        return this
    }

    /**
     * 设置功能范围的名称
     *
     * @param fkbtx 功能范围的名称
     * @return 成本中心
     */
    fun setFkbtx(fkbtx: String?): CostEtItem {
        this.fkbtx = fkbtx
        return this
    }

    override fun toString(): String {
        return json(this)
    }
}