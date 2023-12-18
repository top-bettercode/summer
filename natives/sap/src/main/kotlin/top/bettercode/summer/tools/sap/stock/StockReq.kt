package top.bettercode.summer.tools.sap.stock

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapField
import top.bettercode.summer.tools.sap.annotation.SapStructure
import top.bettercode.summer.tools.sap.connection.pojo.SapHead

class StockReq {
    /**
     * 接口控制数据
     */
    @SapStructure("IS_ZSCRM2_CONTROL")
    var head: SapHead? = null

    /**
     * 物料号
     */
    @SapField("I_MATERIAL")
    var iMaterial: String? = null

    /**
     * 工厂
     */
    @SapField("I_PLANT")
    var iPlant: String? = null

    /**
     * 库存地点
     */
    @SapField("I_STGE_LOC")
    var iStgeLoc: String? = null

    override fun toString(): String {
        return json(this)
    }
}