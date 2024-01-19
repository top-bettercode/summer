package top.bettercode.summer.tools.recipe.material

/**
 *
 * @author Peter Wu
 */
open class MaterialIDs(private val ids: Array<out String>) : Iterable<String> {

    companion object {

        @JvmStatic
        fun of(vararg id: String): MaterialIDs {
            return MaterialIDs(id)
        }

        fun Array<String>.toMaterialIDs(): MaterialIDs {
            return MaterialIDs(this)
        }

        fun Array<String>.toReplacebleMaterialIDs(): ReplacebleMaterialIDs {
            return ReplacebleMaterialIDs(this)
        }
    }

    fun isEmpty(): Boolean {
        return ids.isEmpty()
    }

    fun isNotEmpty(): Boolean {
        return ids.isNotEmpty()
    }

    override operator fun iterator(): Iterator<String> = ids.iterator()

    fun toReplacebleMaterialIDs(replaceId: String, replaceRate: Double): ReplacebleMaterialIDs {
        return ReplacebleMaterialIDs(ids, of(replaceId), replaceRate)
    }


    open fun contains(id: String): Boolean {
        return ids.contains(id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MaterialIDs) return false

        if (!ids.contentEquals(other.ids)) return false

        return true
    }

    override fun hashCode(): Int {
        return ids.contentHashCode()
    }

    override fun toString(): String {
        return ids.joinToString(",")
    }

}