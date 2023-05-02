package top.bettercode.summer.tools.sap.stock

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapField
import java.math.BigDecimal

class StockLsReturn {
    /**
     * @return 可用数量
     */
    /**
     * 可用数量
     */
    @SapField("MENGE_1")
    var menge1 = BigDecimal("0.000")
        private set
    /**
     * @return 库存数量
     */
    /**
     * 库存数量
     */
    @SapField("MENGE_2")
    var menge2 = BigDecimal("0.000")
        private set
    /**
     * @return 基本计量单位
     */
    /**
     * 基本计量单位
     */
    @SapField("MEINS")
    var meins: String? = null
        private set
    /**
     * @return 库存地点
     */
    /**
     * 库存地点
     */
    @SapField("LGORT")
    var lgort: String? = null
        private set

    /**
     * 设置可用数量
     *
     * @param menge1 可用数量
     * @return CRM1 与AP返回库存查询数量
     */
    fun setMenge1(menge1: BigDecimal): StockLsReturn {
        this.menge1 = menge1
        return this
    }

    /**
     * 设置库存数量
     *
     * @param menge2 库存数量
     * @return CRM1 与AP返回库存查询数量
     */
    fun setMenge2(menge2: BigDecimal): StockLsReturn {
        this.menge2 = menge2
        return this
    }

    /**
     * 设置基本计量单位
     *
     * @param meins 基本计量单位
     * @return CRM1 与AP返回库存查询数量
     */
    fun setMeins(meins: String?): StockLsReturn {
        this.meins = meins
        return this
    }

    /**
     * 设置库存地点
     *
     * @param lgort 库存地点
     * @return CRM1 与AP返回库存查询数量
     */
    fun setLgort(lgort: String?): StockLsReturn {
        this.lgort = lgort
        return this
    }

    override fun toString(): String {
        return json(this)
    }
}