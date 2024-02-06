package top.bettercode.summer.tools.recipe.material.id

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 关联原料
 * @author Peter Wu
 */
class RelationMaterialIDs(
        @JsonProperty("ids")
        ids: MutableList<String>,
        /**
         * 关联原料
         */
        @JsonProperty("relationIds")
        val relationIds: MaterialIDs? = null
) : MaterialIDs(ids) {
    constructor(vararg id: String, relationIds: MaterialIDs? = null) : this(id.toMutableList(), relationIds)
    constructor(ids: Iterable<String>, relationIds: MaterialIDs? = null) : this(ids.toMutableList(), relationIds)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RelationMaterialIDs) return false
        if (!super.equals(other)) return false

        if (relationIds != other.relationIds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (relationIds?.hashCode() ?: 0)
        return result
    }


    override fun toString(): String {
        return this.ids.joinToString(",", "[", "]") + "|" + (relationIds?.ids?.joinToString(",", "[", "]")
                ?: "")
    }

}