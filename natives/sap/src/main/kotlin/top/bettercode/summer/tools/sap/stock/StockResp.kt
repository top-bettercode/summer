package top.bettercode.summer.tools.sap.stock

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapStructure
import top.bettercode.summer.tools.sap.connection.pojo.RkEsMessage
import top.bettercode.summer.tools.sap.connection.pojo.SapReturn

class StockResp : SapReturn<RkEsMessage?>() {
    /**
     * CRM1 与AP返回库存查询数量
     */
    @SapStructure("LS_RETURN")
    var lsReturn: StockLsReturn? = null

    override fun toString(): String {
        return json(this)
    }
}