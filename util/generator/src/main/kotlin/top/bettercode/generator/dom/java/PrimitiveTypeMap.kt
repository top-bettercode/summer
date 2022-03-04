package top.bettercode.generator.dom.java

internal object PrimitiveTypeMap {
    private val map = HashMap<String, Class<*>>(9)

    fun getType(var0: String): Class<*>? {
        return map[var0]
    }

    init {
        map[java.lang.Boolean.TYPE.name] = java.lang.Boolean.TYPE
        map[Character.TYPE.name] = Character.TYPE
        map[java.lang.Byte.TYPE.name] = java.lang.Byte.TYPE
        map[java.lang.Short.TYPE.name] = java.lang.Short.TYPE
        map[Integer.TYPE.name] = Integer.TYPE
        map[java.lang.Long.TYPE.name] = java.lang.Long.TYPE
        map[java.lang.Float.TYPE.name] = java.lang.Float.TYPE
        map[java.lang.Double.TYPE.name] = java.lang.Double.TYPE
        map[Void.TYPE.name] = Void.TYPE
    }
}
