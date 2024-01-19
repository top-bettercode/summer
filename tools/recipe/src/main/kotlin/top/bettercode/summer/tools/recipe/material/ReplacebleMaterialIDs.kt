package top.bettercode.summer.tools.recipe.material

/**
 * 可替换物料
 * @author Peter Wu
 */
class ReplacebleMaterialIDs(ids: Array<out String>,
                            /**
                             * 可替换物料
                             */
                            val replaceIds: MaterialIDs? = null,
                            /**
                             * 对可替换物料使用量比例,1单位ids使用replaceRate单位replaceIds
                             */
                            val replaceRate: Double? = null
) : MaterialIDs(ids) {

    override fun contains(id: String): Boolean {
        if (replaceIds?.contains(id) == true) {
            return true
        }
        return super.contains(id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReplacebleMaterialIDs) return false
        if (!super.equals(other)) return false

        if (replaceIds != other.replaceIds) return false
        if (replaceRate != other.replaceRate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (replaceIds?.hashCode() ?: 0)
        result = 31 * result + (replaceRate?.hashCode() ?: 0)
        return result
    }


}