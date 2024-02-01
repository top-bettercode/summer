package top.bettercode.summer.tools.recipe.data

/**
 * 各工厂原料价格,key:工厂ID,value:原料价格
 *
 * @author Peter Wu
 */
class MaterialPriceData : LinkedHashMap<String, Double?>() {
    /**
     * @param factoryId 工厂ID
     * @return 原料价格
     */
    fun getPrice(factoryId: String): Double? {
        return get(factoryId)
    }

    /**
     * @param factoryId 工厂
     * @param price 原料价格
     */
    fun setPrice(factoryId: String, price: Double?) {
        put(factoryId, price)
    }

}
