package top.bettercode.summer.tools.generator.dom.java

import java.util.*

/**
 * The Class JavaType.
 *
 */
open class JavaType(fullTypeSpecification: String) : Comparable<JavaType> {

    /** The short name without any generic arguments.  */
    lateinit var shortNameWithoutTypeArguments: String
        private set

    /** The fully qualified name without any generic arguments.  */
    lateinit var fullyQualifiedNameWithoutTypeParameters: String
        private set

    var isExplicitlyImported: Boolean = false
        private set

    lateinit var packageName: String
        private set

    var isPrimitive: Boolean = false
        private set

    var isArray: Boolean = false
        private set

    /**
     * Gets the primitive type wrapper.
     *
     * @return Returns the wrapperClass.
     */
    private var primitiveTypeWrapper: PrimitiveTypeWrapper? = null

    val typeArguments: MutableList<JavaType>

    // the following three values are used for dealing with wildcard types
    private var wildcardType: Boolean = false

    private var boundedWildcard: Boolean = false

    private var extendsBoundedWildcard: Boolean = false

    init {
        typeArguments = ArrayList()
        parse(fullTypeSpecification)
    }

    /**
     * Returns the fully qualified name - including any generic type parameters.
     *
     * @return Returns the fullyQualifiedName.
     */
    val fullyQualifiedName: String by lazy {
        val sb = StringBuilder()
        if (wildcardType) {
            sb.append('?')
            if (boundedWildcard) {
                if (extendsBoundedWildcard) {
                    sb.append(" extends ")
                } else {
                    sb.append(" super ")
                }

                sb.append(fullyQualifiedNameWithoutTypeParameters)
            }
        } else {
            sb.append(fullyQualifiedNameWithoutTypeParameters)
        }

        if (typeArguments.size > 0) {
            var first = true
            sb.append('<')
            for (fqjt in typeArguments) {
                if (first) {
                    first = false
                } else {
                    sb.append(", ")
                }
                sb.append(fqjt.fullyQualifiedName)

            }
            sb.append('>')
        }
        if (isArray) {
            sb.append("[]")
        }
        sb.toString()
    }

    private fun isAssignableFrom(className: String): Boolean {
        return try {
            val supperClass = Class.forName(fullyQualifiedNameWithoutTypeParameters)
            val clazz = Class.forName(className)
            supperClass.isAssignableFrom(clazz)
        } catch (e: Exception) {
            false
        }
    }

    fun isAssignableFrom(javaType: JavaType): Boolean {
        return isAssignableFrom(javaType.fullyQualifiedNameWithoutTypeParameters)
    }

    fun isEnum(): Boolean {
        return try {
            val clazz = Class.forName(fullyQualifiedNameWithoutTypeParameters)
            clazz.isEnum
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Returns a list of Strings that are the fully qualified names of this type, and any generic type argument
     * associated with this type.
     *
     * @return the import list
     */
    // an inner class is specified, only import the top
    // level class
    val importList: List<String> by lazy {
        val answer = ArrayList<String>()
        if (isExplicitlyImported) {
            val index = shortNameWithoutTypeArguments.indexOf('.')
            if (index == -1) {
                answer.add(calculateActualImport(fullyQualifiedNameWithoutTypeParameters))
            } else {
                val sb = StringBuilder()
                sb.append(packageName)
                sb.append('.')
                sb.append(
                    calculateActualImport(
                        shortNameWithoutTypeArguments.substring(
                            0,
                            index
                        )
                    )
                )
                answer.add(sb.toString())
            }
        }

        for (fqjt in typeArguments) {
            answer.addAll(fqjt.importList)
        }

        answer
    }

    /**
     * Gets the short name.
     *
     * @return Returns the shortName - including any type arguments.
     */
    val shortName: String by lazy {
        val sb = StringBuilder()
        if (wildcardType) {
            sb.append('?')
            if (boundedWildcard) {
                if (extendsBoundedWildcard) {
                    sb.append(" extends ")
                } else {
                    sb.append(" super ")
                }

                sb.append(shortNameWithoutTypeArguments)
            }
        } else {
            sb.append(shortNameWithoutTypeArguments)
        }

        if (typeArguments.size > 0) {
            var first = true
            sb.append('<')
            for (fqjt in typeArguments) {
                if (first) {
                    first = false
                } else {
                    sb.append(", ")
                }
                sb.append(fqjt.shortName)

            }
            sb.append('>')
        }
        if (isArray) {
            sb.append("[]")
        }
        sb.toString()
    }

    private fun calculateActualImport(name: String): String {
        var answer = name
        if (this.isArray) {
            val index = name.indexOf("[")
            if (index != -1) {
                answer = name.substring(0, index)
            }
        }
        return answer
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is JavaType) {
            return false
        }

        return fullyQualifiedName == other.fullyQualifiedName
    }

    override fun hashCode(): Int {
        return fullyQualifiedName.hashCode()
    }

    override fun toString(): String {
        return fullyQualifiedName
    }

    override fun compareTo(other: JavaType): Int {
        return fullyQualifiedName.compareTo(other.fullyQualifiedName)
    }

    fun typeArgument(vararg type: JavaType): JavaType {
        typeArguments.addAll(type)
        return this
    }

    fun typeArgument(vararg fullTypeSpecification: String): JavaType {
        typeArguments.addAll(fullTypeSpecification.map { JavaType(it) })
        return this
    }

    private fun parse(fullTypeSpecification: String) {
        var spec = fullTypeSpecification.trim { it <= ' ' }

        if (spec.startsWith("?")) {
            wildcardType = true
            spec = spec.substring(1).trim { it <= ' ' }
            when {
                spec.startsWith("extends ") -> {
                    boundedWildcard = true
                    extendsBoundedWildcard = true
                    spec = spec.substring(8)  // "extends ".columnSize()
                }

                spec.startsWith("super ") -> {
                    boundedWildcard = true
                    extendsBoundedWildcard = false
                    spec = spec.substring(6)  // "super ".columnSize()
                }

                else -> {
                    boundedWildcard = false
                }
            }
            parse(spec)
        } else {
            val index = fullTypeSpecification.indexOf('<')
            if (index == -1) {
                simpleParse(fullTypeSpecification.substringBefore('['))
            } else {
                simpleParse(fullTypeSpecification.substring(0, index))
                val endIndex = fullTypeSpecification.lastIndexOf('>')
                if (endIndex == -1) {
                    throw RuntimeException(fullTypeSpecification)
                }
                genericParse(fullTypeSpecification.substring(index, endIndex + 1))
            }

            // this is far from a perfect test for detecting arrays, but is close
            // enough for most cases.  It will not detect an improperly specified
            // array type like byte], but it will detect byte[] and byte[   ]
            // which are both valid
            isArray = fullTypeSpecification.endsWith("]")
        }
    }

    private fun simpleParse(typeSpecification: String) {
        fullyQualifiedNameWithoutTypeParameters = typeSpecification.trim { it <= ' ' }
        if (fullyQualifiedNameWithoutTypeParameters.contains(".")) {
            packageName = fullyQualifiedNameWithoutTypeParameters.substringBeforeLast(".")
            shortNameWithoutTypeArguments = fullyQualifiedNameWithoutTypeParameters
                .substring(packageName.length + 1)
            val index = shortNameWithoutTypeArguments.lastIndexOf('.')
            if (index != -1) {
                shortNameWithoutTypeArguments = shortNameWithoutTypeArguments.substring(index + 1)
            }

            isExplicitlyImported = JAVA_LANG != packageName
        } else {
            shortNameWithoutTypeArguments = fullyQualifiedNameWithoutTypeParameters
            isExplicitlyImported = false
            packageName = ""

            when (fullyQualifiedNameWithoutTypeParameters) {
                "byte" -> {
                    isPrimitive = true
                    primitiveTypeWrapper = PrimitiveTypeWrapper.byteInstance
                }

                "short" -> {
                    isPrimitive = true
                    primitiveTypeWrapper = PrimitiveTypeWrapper.shortInstance
                }

                "int" -> {
                    isPrimitive = true
                    primitiveTypeWrapper = PrimitiveTypeWrapper.integerInstance
                }

                "long" -> {
                    isPrimitive = true
                    primitiveTypeWrapper = PrimitiveTypeWrapper.longInstance
                }

                "char" -> {
                    isPrimitive = true
                    primitiveTypeWrapper = PrimitiveTypeWrapper.characterInstance
                }

                "float" -> {
                    isPrimitive = true
                    primitiveTypeWrapper = PrimitiveTypeWrapper.floatInstance
                }

                "double" -> {
                    isPrimitive = true
                    primitiveTypeWrapper = PrimitiveTypeWrapper.doubleInstance
                }

                "boolean" -> {
                    isPrimitive = true
                    primitiveTypeWrapper = PrimitiveTypeWrapper.booleanInstance
                }

                else -> {
                    isPrimitive = false
                    primitiveTypeWrapper = null
                }
            }
        }
    }

    private fun genericParse(genericSpecification: String) {
        val lastIndex = genericSpecification.lastIndexOf('>')
        if (lastIndex == -1) {
            // shouldn't happen - should be caught already, but just in case...
            throw RuntimeException(genericSpecification)
        }
        val argumentString = genericSpecification.substring(1, lastIndex)
        // need to find "," outside of a <> bounds
        val st = StringTokenizer(argumentString, ",<>", true)
        var openCount = 0
        val sb = StringBuilder()
        while (st.hasMoreTokens()) {
            val token = st.nextToken()
            if ("<" == token) {
                sb.append(token)
                openCount++
            } else if (">" == token) {
                sb.append(token)
                openCount--
            } else if ("," == token) {
                if (openCount == 0) {
                    typeArguments
                        .add(JavaType(sb.toString()))
                    sb.setLength(0)
                } else {
                    sb.append(token)
                }
            } else {
                sb.append(token)
            }
        }

        if (openCount != 0) {
            throw RuntimeException(genericSpecification)
        }

        val finalType = sb.toString()
        if (finalType.isNotBlank()) {
            typeArguments.add(JavaType(finalType))
        }
    }


    companion object {

        private const val JAVA_LANG = "java.lang"

        val voidPrimitiveInstance: JavaType by lazy { JavaType("void") }
        val intPrimitiveInstance: JavaType by lazy { JavaType("int") }
        val longPrimitiveInstance: JavaType by lazy { JavaType("long") }
        val booleanPrimitiveInstance: JavaType by lazy { JavaType("boolean") }
        val charPrimitiveInstance: JavaType by lazy { JavaType("char") }
        val bytePrimitiveInstance: JavaType by lazy { JavaType("byte") }
        val shortPrimitiveInstance: JavaType by lazy { JavaType("short") }
        val floatPrimitiveInstance: JavaType by lazy { JavaType("float") }
        val doublePrimitiveInstance: JavaType by lazy { JavaType("double") }

        // always return a new instance because the type may be parameterized

        val mapInstance: JavaType by lazy { JavaType("java.util.Map") }

        // always return a new instance because the type may be parameterized

        val listInstance: JavaType by lazy { JavaType("java.util.List") }

        // always return a new instance because the type may be parameterized

        val hashMapInstance: JavaType by lazy { JavaType("java.util.HashMap") }

        // always return a new instance because the type may be parameterized

        val arrayListInstance: JavaType by lazy { JavaType("java.util.ArrayList") }

        // always return a new instance because the type may be parameterized
        val iteratorInstance: JavaType by lazy { JavaType("java.util.Iterator") }

        val stringInstance: JavaType by lazy { JavaType("java.lang.String") }


        val objectInstance: JavaType by lazy { JavaType("java.lang.Object") }

        val dateInstance: JavaType by lazy { JavaType("java.util.Date") }

        val criteriaInstance: JavaType by lazy { JavaType("Criteria") }

        val generatedCriteriaInstance: JavaType by lazy { JavaType("GeneratedCriteria") }

    }
}
