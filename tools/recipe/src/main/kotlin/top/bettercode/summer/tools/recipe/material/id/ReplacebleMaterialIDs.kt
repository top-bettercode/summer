package top.bettercode.summer.tools.recipe.material.id

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 可替换原料
 * @author Peter Wu
 */
class ReplacebleMaterialIDs(
        @JsonProperty("ids")
        ids: List<String>,
        /**
         * 对可替换原料使用量比例,1单位ids使用replaceRate单位replaceIds
         */
        @JsonProperty("replaceRate")
        val replaceRate: Double? = null,

        /**
         * 可替换原料
         */
        @JsonProperty("replaceIds")
        val replaceIds: MaterialIDs? = null,

        ) : MaterialIDs(ids) {
    constructor(vararg id: String, replaceRate: Double? = null, replaceIds: MaterialIDs? = null) : this(id.toList(), replaceRate, replaceIds)
    constructor(ids: Iterable<String>, replaceRate: Double? = null, replaceIds: MaterialIDs? = null) : this(ids.toList(), replaceRate, replaceIds)

    override fun contains(element: String): Boolean {
        return if (replaceIds?.contains(element) == true) {
            true
        } else
            super.contains(element)
    }

    override fun containsAll(elements: Collection<String>): Boolean {
        return if (replaceIds == null)
            super.containsAll(elements)
        else {
            (ids + replaceIds).containsAll(elements)
        }
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

    override fun toString(): String {
        return this.ids.joinToString(",", "[", "]") + "|${replaceRate?.toString() ?: ""}" + "|" + (replaceIds?.ids?.joinToString(",", "[", "]")
                ?: "")
    }

}