package cn.bestwu.generator.dsl

import java.io.Serializable

/**
 * @author Peter Wu
 */
class DicCodes(
    val type: String,
    val name: String,
    var isInt: Boolean = false,
    val codes: MutableMap<Serializable, String> = mutableMapOf()
) : Serializable