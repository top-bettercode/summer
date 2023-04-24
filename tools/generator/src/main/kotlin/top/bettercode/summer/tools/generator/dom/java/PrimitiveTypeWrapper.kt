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
private constructor(
        fullyQualifiedName: String,
        val primitiveType: JavaType,
        private val toPrimitiveMethod: String
) : JavaType(fullyQualifiedName) {
    companion object {
        val booleanInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper(
                "java.lang.Boolean",
                JavaType.booleanPrimitiveInstance,
                "booleanValue()"
        )
        val byteInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper(
                "java.lang.Byte",
                JavaType.bytePrimitiveInstance,
                "byteValue()"
        )
        val characterInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper(
                "java.lang.Character",
                JavaType.charPrimitiveInstance,
                "charValue()"
        )
        val doubleInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper(
                "java.lang.Double",
                JavaType.doublePrimitiveInstance,
                "doubleValue()"
        )
        val floatInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper(
                "java.lang.Float",
                JavaType.floatPrimitiveInstance,
                "floatValue()"
        )
        val integerInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper(
                "java.lang.Integer",
                JavaType.intPrimitiveInstance,
                "intValue()"
        )
        val longInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper(
                "java.lang.Long",
                JavaType.longPrimitiveInstance,
                "longValue()"
        )
        val shortInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper(
                "java.lang.Short",
                JavaType.shortPrimitiveInstance,
                "shortValue()"
        )
        val voidInstance: PrimitiveTypeWrapper = PrimitiveTypeWrapper(
                "java.lang.Void",
                JavaType.voidPrimitiveInstance,
                ""
        )
    }
}
