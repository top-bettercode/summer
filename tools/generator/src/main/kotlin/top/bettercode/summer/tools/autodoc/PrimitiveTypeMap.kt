package top.bettercode.summer.tools.autodoc

internal object PrimitiveTypeMap {
    private val map = HashMap<String, String>(9)

    fun getType(var0: String): String {
        return map[var0] ?: var0
    }

    init {
        map["boolean"] = java.lang.Boolean.TYPE.name
        map["char"] = Character.TYPE.name
        map["byte"] = java.lang.Byte.TYPE.name
        map["short"] = java.lang.Short.TYPE.name
        map["int"] = Integer.TYPE.name
        map["long"] = java.lang.Long.TYPE.name
        map["float"] = java.lang.Float.TYPE.name
        map["double"] = java.lang.Double.TYPE.name
        map["void"] = Void.TYPE.name
    }
}
