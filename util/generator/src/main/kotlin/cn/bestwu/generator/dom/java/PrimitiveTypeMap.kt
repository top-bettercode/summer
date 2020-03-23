package cn.bestwu.generator.dom.java

import java.util.*

internal object PrimitiveTypeMap {
    private val map = HashMap<String, Class<*>>(9)

    fun getType(var0: String): Class<*>? {
        return map[var0]
    }

    init {
        map[java.lang.Boolean.TYPE.name] = java.lang.Boolean.TYPE
        map[java.lang.Character.TYPE.name] = java.lang.Character.TYPE
        map[java.lang.Byte.TYPE.name] = java.lang.Byte.TYPE
        map[java.lang.Short.TYPE.name] = java.lang.Short.TYPE
        map[java.lang.Integer.TYPE.name] = java.lang.Integer.TYPE
        map[java.lang.Long.TYPE.name] = java.lang.Long.TYPE
        map[java.lang.Float.TYPE.name] = java.lang.Float.TYPE
        map[java.lang.Double.TYPE.name] = java.lang.Double.TYPE
        map[java.lang.Void.TYPE.name] = java.lang.Void.TYPE
    }
}
