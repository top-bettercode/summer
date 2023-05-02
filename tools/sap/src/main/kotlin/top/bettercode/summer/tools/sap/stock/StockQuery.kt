package top.bettercode.summer.tools.sap.stock

import top.bettercode.summer.tools.lang.util.StringUtil.json
import java.util.*

/**
 * @author Peter Wu
 */
class StockQuery(var factoryId: String, var materialId: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is StockQuery) {
            return false
        }
        val that = other
        return factoryId == that.factoryId && materialId == that.materialId
    }

    override fun hashCode(): Int {
        return Objects.hash(factoryId, materialId)
    }

    override fun toString(): String {
        return json(this, true)
    }
}
