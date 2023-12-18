package top.bettercode.summer.tools.sap.stock

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapField
import java.math.BigDecimal

class StockLsReturn {
    /**
     * 可用数量
     */
    @SapField("MENGE_1")
    var menge1 = BigDecimal("0.000")

    /**
     * 库存数量
     */
    @SapField("MENGE_2")
    var menge2 = BigDecimal("0.000")

    /**
     * 基本计量单位
     */
    @SapField("MEINS")
    var meins: String? = null

    /**
     * 库存地点
     */
    @SapField("LGORT")
    var lgort: String? = null

    override fun toString(): String {
        return json(this)
    }
}