package top.bettercode.summer.tools.sap.stock

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapStructure
import top.bettercode.summer.tools.sap.connection.pojo.RkEsMessage
import top.bettercode.summer.tools.sap.connection.pojo.SapReturn

class StockResp : SapReturn<RkEsMessage?>() {
    /**
     * @return CRM1 与AP返回库存查询数量
     */
    /**
     * CRM1 与AP返回库存查询数量
     */
    @SapStructure("LS_RETURN")
    var lsReturn: StockLsReturn? = null
        private set

    /**
     * 设置CRM1 与AP返回库存查询数量
     *
     * @param lsReturn CRM1 与AP返回库存查询数量
     * @return StockResp
     */
    fun setLsReturn(lsReturn: StockLsReturn?): StockResp {
        this.lsReturn = lsReturn
        return this
    }

    override fun toString(): String {
        return json(this)
    }
}