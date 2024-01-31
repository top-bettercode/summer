package top.bettercode.summer.tools.recipe.material

/**
 * 关联物料
 * @author Peter Wu
 */
class RelationMaterialIDs(ids: HashSet<String>,
                          /**
                           * 关联物料
                           */
                          val relationIds: MaterialIDs? = null
) : MaterialIDs(ids) {
    constructor(vararg id: String, relationIds: MaterialIDs? = null) : this(id.toHashSet(), relationIds)
    constructor(ids: Iterable<String>, relationIds: MaterialIDs? = null) : this(ids.toHashSet(), relationIds)


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
        return this.joinToString(",", "[", "]") + (if (relationIds != null) "\n" + relationIds.joinToString(",", "[", "]") else "")
    }

}