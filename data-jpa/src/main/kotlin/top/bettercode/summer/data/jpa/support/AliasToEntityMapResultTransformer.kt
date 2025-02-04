package top.bettercode.summer.data.jpa.support

import org.hibernate.transform.AliasedTupleSubsetResultTransformer

class AliasToEntityMapResultTransformer
/**
 * Disallow instantiation of AliasToEntityMapResultTransformer.
 */
private constructor() : AliasedTupleSubsetResultTransformer() {
    override fun transformTuple(tuple: Array<Any?>, aliases: Array<String?>): Any {
        val result = LinkedHashMap<Any?, Any?>(tuple.size)
        aliases.forEachIndexed { index, alias ->
            if (alias != null) {
                result[alias] = tuple[index]
            }
        }
        return result
    }

    override fun isTransformedValueATupleElement(
        aliases: Array<String>,
        tupleLength: Int
    ): Boolean {
        return false
    }

    /**
     * Serialization hook for ensuring singleton uniqueing.
     *
     * @return The singleton instance : [.INSTANCE]
     */
    private fun readResolve(): Any {
        return INSTANCE
    }

    companion object {
        val INSTANCE: AliasToEntityMapResultTransformer = AliasToEntityMapResultTransformer()
    }
}
