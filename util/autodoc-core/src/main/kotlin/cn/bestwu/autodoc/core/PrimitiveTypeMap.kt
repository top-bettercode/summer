package cn.bestwu.autodoc.core

import java.util.*

internal object PrimitiveTypeMap {
    private val map = HashMap<String, String>(9)

    fun getType(var0: String): String {
        return map[var0] ?: var0
    }

    init {
        map["boolean"] = java.lang.Boolean.TYPE.name
        map["char"] = java.lang.Character.TYPE.name
        map["byte"] = java.lang.Byte.TYPE.name
        map["short"] = java.lang.Short.TYPE.name
        map["int"] = java.lang.Integer.TYPE.name
        map["long"] = java.lang.Long.TYPE.name
        map["float"] = java.lang.Float.TYPE.name
        map["double"] = java.lang.Double.TYPE.name
        map["void"] = java.lang.Void.TYPE.name
    }
}