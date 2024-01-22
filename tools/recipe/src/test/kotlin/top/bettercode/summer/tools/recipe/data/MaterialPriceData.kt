package top.bettercode.summer.tools.recipe.data

/**
 * 各工厂物料价格,key:工厂ID,value:物料价格
 *
 * @author Peter Wu
 */
class MaterialPriceData : LinkedHashMap<String, Double?>() {
    /**
     * @param factoryId 工厂ID
     * @return 物料价格
     */
    fun getPrice(factoryId: String): Double? {
        return get(factoryId)
    }

    /**
     * @param factoryId 工厂
     * @param price 物料价格
     */
    fun setPrice(factoryId: String, price: Double?) {
        put(factoryId, price)
    }

}
