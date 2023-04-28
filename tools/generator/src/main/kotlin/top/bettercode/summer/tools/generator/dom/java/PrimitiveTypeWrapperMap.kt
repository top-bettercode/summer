package top.bettercode.summer.tools.generator.dom.java

internal object PrimitiveTypeWrapperMap {
    private val map = HashMap<String, JavaType?>(9)

    fun getType(var0: String): JavaType? {
        return map[var0]
    }

    init {
        map[java.lang.Boolean::class.java.name] = JavaType.booleanPrimitiveInstance
        map[Character::class.java.name] = JavaType.charPrimitiveInstance
        map[java.lang.Byte::class.java.name] = JavaType.bytePrimitiveInstance
        map[java.lang.Short::class.java.name] = JavaType.shortPrimitiveInstance
        map[Integer::class.java.name] = JavaType.intPrimitiveInstance
        map[java.lang.Long::class.java.name] = JavaType.longPrimitiveInstance
        map[java.lang.Float::class.java.name] = JavaType.floatPrimitiveInstance
        map[java.lang.Double::class.java.name] = JavaType.doublePrimitiveInstance
        map[Void::class.java.name] = JavaType.voidPrimitiveInstance
    }
}
