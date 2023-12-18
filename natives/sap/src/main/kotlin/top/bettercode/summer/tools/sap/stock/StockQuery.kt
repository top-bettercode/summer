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
        return factoryId == other.factoryId && materialId == other.materialId
    }

    override fun hashCode(): Int {
        return Objects.hash(factoryId, materialId)
    }

    override fun toString(): String {
        return json(this, true)
    }
}
