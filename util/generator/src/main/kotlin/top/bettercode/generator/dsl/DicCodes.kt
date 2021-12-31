package top.bettercode.generator.dsl

import java.io.Serializable

/**
 * @author Peter Wu
 */
class DicCodes(
    val type: String,
    val name: String,
    var isInt: Boolean = false,
    val codes: MutableMap<Serializable, String> = mutableMapOf()
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}