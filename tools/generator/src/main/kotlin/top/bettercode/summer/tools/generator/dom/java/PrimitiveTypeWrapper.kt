package top.bettercode.summer.tools.generator.dom.java

class PrimitiveTypeWrapper
/**
 * Use the static getXXXInstance methods to gain access to one of the type
 * wrappers.
 *
 * @param fullyQualifiedName
 * fully qualified name of the wrapper type
 * @param toPrimitiveMethod
 * the method that returns the wrapped primitive
 */
private constructor(fullyQualifiedName: String,
                    private val toPrimitiveMethod: String) : JavaType(fullyQualifiedName) {
    companion object {
        val booleanInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper("java.lang.Boolean",
                "booleanValue()")
        val byteInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper("java.lang.Byte",
                "byteValue()")
        val characterInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper("java.lang.Character",
                "charValue()")
        val doubleInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper("java.lang.Double",
                "doubleValue()")
        val floatInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper("java.lang.Float",
                "floatValue()")
        val integerInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper("java.lang.Integer",
                "intValue()")
        val longInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper("java.lang.Long",
                "longValue()")
        val shortInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper("java.lang.Short",
                "shortValue()")
        val voidInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper("java.lang.Void",
                "")
    }
}
