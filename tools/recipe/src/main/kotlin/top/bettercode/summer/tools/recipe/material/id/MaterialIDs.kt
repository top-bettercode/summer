package top.bettercode.summer.tools.recipe.material.id

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @author Peter Wu
 */
open class MaterialIDs(
         ids: MutableList<String>) : Comparable<MaterialIDs>, Iterable<String> {
    constructor(vararg id: String) : this(id.toMutableList())
    constructor(ids: Iterable<String>) : this(ids.toMutableList())

    @JsonProperty("ids")
    val ids: MutableList<String> = ids.distinct().toMutableList()

    companion object {
        fun Array<String>.toMaterialIDs(): MaterialIDs {
            return MaterialIDs(*this)
        }

        fun Array<String>.toReplacebleMaterialIDs(): ReplacebleMaterialIDs {
            return ReplacebleMaterialIDs(*this)
        }

        fun Array<String>.toRelationMaterialIDs(relationIds: MaterialIDs? = null): RelationMaterialIDs {
            return RelationMaterialIDs(id = this, relationIds = relationIds)
        }

        fun Iterable<String>.toMaterialIDs(): MaterialIDs {
            return MaterialIDs(this)
        }

        fun Iterable<String>.toReplacebleMaterialIDs(): ReplacebleMaterialIDs {
            return ReplacebleMaterialIDs(this)
        }

        fun Iterable<String>.toRelationMaterialIDs(relationIds: MaterialIDs? = null): RelationMaterialIDs {
            return RelationMaterialIDs(this, relationIds)
        }

    }

    @JvmOverloads
    fun relation(relationIds: MaterialIDs? = null): RelationMaterialIDs {
        return RelationMaterialIDs(ids, relationIds)
    }

    fun replace(replaceRate: Double, replaceId: String): ReplacebleMaterialIDs {
        return ReplacebleMaterialIDs(ids, replaceRate, MaterialIDs(replaceId))
    }

    fun replace(): ReplacebleMaterialIDs {
        return ReplacebleMaterialIDs(ids, null, null)
    }

    open fun contains(element: String): Boolean {
        return ids.contains(element)
    }

    open fun containsAll(elements: Collection<String>): Boolean {
        return ids.containsAll(elements)
    }

    fun idsString(): String = if (ids.size == 1) ids.first() else ids.joinToString(",", "[", "]")

    override fun toString(): String {
        return idsString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MaterialIDs) return false

        if (ids.toSet() != other.ids.toSet()) return false

        return true
    }

    override fun hashCode(): Int {
        return ids.toSet().hashCode()
    }

    override fun compareTo(other: MaterialIDs): Int {
        return toString().compareTo(other.toString())
    }

    override operator fun iterator(): MutableIterator<String> = ids.iterator()
}