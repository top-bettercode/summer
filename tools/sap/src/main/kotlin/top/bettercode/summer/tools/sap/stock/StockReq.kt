package top.bettercode.summer.tools.sap.stock

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapField
import top.bettercode.summer.tools.sap.annotation.SapStructure
import top.bettercode.summer.tools.sap.connection.pojo.SapHead

class StockReq {
    /**
     * @return 接口控制数据
     */
    /**
     * 接口控制数据
     */
    @SapStructure("IS_ZSCRM2_CONTROL")
    var head: SapHead? = null
        private set
    /**
     * @return 物料号
     */
    /**
     * 物料号
     */
    @SapField("I_MATERIAL")
    var iMaterial: String? = null
        private set
    /**
     * @return 工厂
     */
    /**
     * 工厂
     */
    @SapField("I_PLANT")
    var iPlant: String? = null
        private set
    /**
     * @return 库存地点
     */
    /**
     * 库存地点
     */
    @SapField("I_STGE_LOC")
    var iStgeLoc: String? = null
        private set

    /**
     * 设置接口控制数据
     *
     * @param head 接口控制数据
     * @return StockReq
     */
    fun setHead(head: SapHead?): StockReq {
        this.head = head
        return this
    }

    /**
     * 设置物料号
     *
     * @param iMaterial 物料号
     * @return StockReq
     */
    fun setIMaterial(iMaterial: String?): StockReq {
        this.iMaterial = iMaterial
        return this
    }

    /**
     * 设置工厂
     *
     * @param iPlant 工厂
     * @return StockReq
     */
    fun setIPlant(iPlant: String?): StockReq {
        this.iPlant = iPlant
        return this
    }

    /**
     * 设置库存地点
     *
     * @param iStgeLoc 库存地点
     * @return StockReq
     */
    fun setIStgeLoc(iStgeLoc: String?): StockReq {
        this.iStgeLoc = iStgeLoc
        return this
    }

    override fun toString(): String {
        return json(this)
    }
}