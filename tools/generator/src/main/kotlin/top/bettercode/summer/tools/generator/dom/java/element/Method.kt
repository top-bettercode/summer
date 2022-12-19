package top.bettercode.summer.tools.generator.dom.java.element

import top.bettercode.summer.tools.generator.dom.java.JavaDomUtils
import top.bettercode.summer.tools.generator.dom.java.JavaType


/**
 * The Class Method.
 *
 */
class Method : JavaElement() {

    /** The body lines.  */
    private val bodyLines: MutableList<String> = mutableListOf()
    var interfaceMethod: Boolean = false
    /** The constructor.  */
    /**
     * Sets the constructor.
     *
     * The constructor to set.
     */
    var isConstructor: Boolean = false

    /**
     * Sets the return type.
     *
     * The returnType to set.
     */
    var returnType: JavaType = JavaType.voidPrimitiveInstance

    /** The name.  */
    /**
     * Sets the name.
     *
     * The name to set.
     */
    lateinit var name: String

    /** The type parameters.  */
    private val typeParameters: MutableList<TypeParameter> = mutableListOf()

    /** The parameters.  */
    val parameters: MutableList<Parameter> = mutableListOf()

    /** The exceptions.  */
    private val exceptions: MutableList<JavaType> = mutableListOf()

    /** The is synchronized.  */
    /**
     * Sets the synchronized.
     *
     * the new synchronized
     */
    var isSynchronized: Boolean = false

    /** The is native.  */
    var isNative: Boolean = false
    var isAbstract: Boolean = false

    var isDefault: Boolean = false

    var defaultString: String? = null

    fun parameter(closure: Parameter.() -> Unit) {
        val parameter = Parameter()
        closure(parameter)
        parameters.add(parameter)
    }

    fun parameter(type: JavaType, name: String) {
        val parameter = Parameter()
        parameter.type = type
        parameter.name = name
        parameters.add(parameter)
    }

    operator fun String.unaryPlus() {
        bodyLines.add(this)
    }

    /**
     * indent this(Int) level
     */
    operator fun Int.plus(str: String) {
        var prefix = ""
        for (i in 0 until this) {
            prefix += indent
        }
        bodyLines.add(prefix + str)
    }

    /**
     * Adds the body line.
     *
     * @param line
     * the line
     */
    fun bodyLine(line: String) {
        bodyLines.add(line)
    }

    /**
     * Adds the type parameter.
     *
     * @param typeParameter
     * the type parameter
     */
    fun typeParameter(vararg typeParameter: TypeParameter) {
        typeParameters.addAll(typeParameter)
    }

    /**
     * Adds the parameter.
     *
     * @param parameter
     * the parameter
     */
    fun parameter(vararg parameter: Parameter) {
        parameters.addAll(parameter)
    }

    /**
     * Adds the exception.
     *
     * @param exception
     * the exception
     */
    fun exception(exception: JavaType) {
        exceptions.add(exception)
    }

    /**
     * Gets the formatted content.
     *
     * @param indentLevel
     * the interface method
     * @param compilationUnit the compilation unit
     * @return the formatted content
     */
    override fun getFormattedContent(indentLevel: Int, compilationUnit: CompilationUnit): String {
        var indentLevelInner = indentLevel
        val sb = StringBuilder()

        addFormattedJavadoc(sb, indentLevelInner)
        addFormattedAnnotations(sb, indentLevelInner)

        indent(sb, indentLevelInner)

        if (interfaceMethod) {
            if (isStatic) {
                sb.append("static ")
            } else if (isDefault) {
                sb.append("default ")
            }
        } else {
            sb.append(visibility.value)

            if (isStatic) {
                sb.append("static ")
            }

            if (isFinal) {
                sb.append("final ")
            }

            if (isSynchronized) {
                sb.append("synchronized ")
            }

            if (isNative) {
                sb.append("native ")
            } else if (isAbstract) {
                sb.append("abstract ")
            }
        }

        if (typeParameters.isNotEmpty()) {
            sb.append("<")
            var comma = false
            for (typeParameter in typeParameters) {
                if (comma) {
                    sb.append(", ")
                } else {
                    comma = true
                }

                sb.append(typeParameter.getFormattedContent(compilationUnit))
            }
            sb.append("> ")
        }

        if (!isConstructor) {
            sb.append(JavaDomUtils.calculateTypeName(compilationUnit, returnType))
            sb.append(' ')
        }

        sb.append(name)
        sb.append('(')

        var comma = false
        for (parameter in parameters) {
            if (comma) {
                sb.append(", ")
            } else {
                comma = true
            }

            sb.append(parameter.getFormattedContent(compilationUnit))
        }

        sb.append(')')

        if (exceptions.size > 0) {
            sb.append(" throws ")
            comma = false
            for (fqjt in exceptions) {
                if (comma) {
                    sb.append(", ")
                } else {
                    comma = true
                }

                sb.append(JavaDomUtils.calculateTypeName(compilationUnit, fqjt))
            }
        }

        if (isAbstract || isNative || (interfaceMethod && !isDefault)) {
            if (!defaultString.isNullOrBlank())
                sb.append(" default ${if (returnType == JavaType.stringInstance) "\"${defaultString}\"" else defaultString}")
            sb.append(';')
        } else {
            sb.append(" {")
            indentLevelInner++

            val listIter = bodyLines.listIterator()
            while (listIter.hasNext()) {
                val line = listIter.next()
                if (line.startsWith("}")) {
                    indentLevelInner--
                }

                newLine(sb)
                indent(sb, indentLevelInner)
                sb.append(line)

                if (line.endsWith("{") && !line.startsWith("switch")  //$NON-NLS-2$
                    || line.endsWith(":")
                ) {
                    indentLevelInner++
                }

                if (line.startsWith("break")) {
                    // if the next line is '}', then don't outdent
                    if (listIter.hasNext()) {
                        val nextLine = listIter.next()
                        if (nextLine.startsWith("}")) {
                            indentLevelInner++
                        }

                        // set back to the previous element
                        listIter.previous()
                    }
                    indentLevelInner--
                }
            }

            indentLevelInner--
            newLine(sb)
            indent(sb, indentLevelInner)
            sb.append('}')
        }

        return sb.toString()
    }
}