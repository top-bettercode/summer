package top.bettercode.summer.tools.recipe.material

/**
 *
 * @author Peter Wu
 */
open class MaterialIDs(private val ids: HashSet<String>) : Set<String> by ids {
    constructor(vararg id: String) : this(id.toHashSet())
    constructor(ids: Iterable<String>) : this(ids.toHashSet())

    companion object {

        fun Array<String>.toMaterialIDs(): MaterialIDs {
            return MaterialIDs(*this)
        }

        fun Array<String>.toReplacebleMaterialIDs(): ReplacebleMaterialIDs {
            return ReplacebleMaterialIDs(*this)
        }

        fun Iterable<String>.toMaterialIDs(): MaterialIDs {
            return MaterialIDs(this)
        }

        fun Iterable<String>.toReplacebleMaterialIDs(): ReplacebleMaterialIDs {
            return ReplacebleMaterialIDs(this)
        }
    }

    fun toReplacebleMaterialIDs(replaceId: String, replaceRate: Double): ReplacebleMaterialIDs {
        return ReplacebleMaterialIDs(this, MaterialIDs(replaceId), replaceRate)
    }

    override fun toString(): String {
        return ids.joinToString(",", "[", "]")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MaterialIDs) return false

        if (ids != other.ids) return false

        return true
    }

    override fun hashCode(): Int {
        return ids.hashCode()
    }


}