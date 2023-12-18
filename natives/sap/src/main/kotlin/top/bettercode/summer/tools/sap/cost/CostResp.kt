package top.bettercode.summer.tools.sap.cost

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapTable
import top.bettercode.summer.tools.sap.connection.pojo.EtReturns

class CostResp : EtReturns() {
    /**
     * 成本中心
     */
    @SapTable("ET_ITEM")
    var etItem: List<CostEtItem>? = null

    /**
     * KOSTL的范围
     */
    @SapTable("SO_KOSTL")
    var soKostl: List<CostSoKostl>? = null


    override fun toString(): String {
        return json(this)
    }
}