package cn.bestwu.generator.dsl

import java.io.Serializable

/**
 * @author Peter Wu
 */
class DicCodes(
    val type: String,
    val name: String,
    val codes: MutableMap<String, String> = mutableMapOf()
) : Serializable