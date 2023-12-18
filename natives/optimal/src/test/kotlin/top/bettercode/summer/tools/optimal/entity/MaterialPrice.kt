package top.bettercode.summer.tools.optimal.entity

/**
 * 各工厂原料价格
 *
 * @author Peter Wu
 */
class MaterialPrice : LinkedHashMap<String, Long?>() {
    /**
     * @param factory 工厂
     * @return 材料价格, 单位: 元/kg
     */
    fun getPrice(factory: String): Long? {
        return get(factory)
    }

    /**
     * @param factory 工厂
     * @param price 材料价格, 单位: 元/kg
     */
    fun setPrice(factory: String, price: Long?) {
        put(factory, price)
    }

    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID = 1L
    }
}
