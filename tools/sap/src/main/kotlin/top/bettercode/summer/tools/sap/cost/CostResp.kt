package top.bettercode.summer.tools.sap.cost

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapTable
import top.bettercode.summer.tools.sap.connection.pojo.EtReturns

class CostResp : EtReturns() {
    /**
     * @return 成本中心
     */
    /**
     * 成本中心
     */
    @SapTable("ET_ITEM")
    var etItem: List<CostEtItem>? = null
        private set
    /**
     * @return KOSTL的范围
     */
    /**
     * KOSTL的范围
     */
    @SapTable("SO_KOSTL")
    var soKostl: List<CostSoKostl>? = null
        private set

    /**
     * 设置成本中心
     *
     * @param etItem 成本中心
     * @return CostResp
     */
    fun setEtItem(etItem: List<CostEtItem>?): CostResp {
        this.etItem = etItem
        return this
    }

    /**
     * 设置KOSTL的范围
     *
     * @param soKostl KOSTL的范围
     * @return CostResp
     */
    fun setSoKostl(soKostl: List<CostSoKostl>?): CostResp {
        this.soKostl = soKostl
        return this
    }

    override fun toString(): String {
        return json(this)
    }
}