package top.bettercode.summer.tools.generator.dom.java.element

import top.bettercode.summer.tools.generator.dom.java.JavaDomUtils
import top.bettercode.summer.tools.generator.dom.java.JavaType

/**
 * This class encapsulates the idea of an inner enum - it has methods that make
 * it easy to generate inner enum.
 *
 */
open class InnerEnum(type: JavaType) : InnerUnit(type) {

    /** The inner interfaces.  */
    protected val innerInterfaces: MutableList<InnerInterface> = mutableListOf()

    /** The inner classes.  */
    protected val innerClasses: MutableList<InnerClass> = mutableListOf()

    /** The inner enums.  */
    protected val innerEnums: MutableList<InnerEnum> = mutableListOf()


    /** The enum constants.  */
    private val enumConstants: MutableList<EnumField> = mutableListOf()


    fun enumConstant(name: String, ef: EnumField.() -> Unit) {
        val field = EnumField(name)
        field.ef()
        enumConstant(field)
    }

    override fun calculateImports(importedTypes: MutableSet<JavaType>): Set<String> {
        innerInterfaces.forEach {
            it.calculateImports(importedTypes)
        }
        innerClasses.forEach {
            it.calculateImports(importedTypes)
        }
        innerEnums.forEach {
            it.calculateImports(importedTypes)
        }
        return super.calculateImports(importedTypes)
    }

    /**
     * Gets the formatted content.
     *
     * @param indentLevel
     * the indent level
     * @param compilationUnit the compilation unit
     * @return the formatted content
     */
    override fun getFormattedContent(indentLevel: Int, compilationUnit: CompilationUnit): String {
        var indentLevelInner = indentLevel
        val sb = StringBuilder()

        addFormattedJavadoc(sb, indentLevelInner)
        addFormattedAnnotations(sb, indentLevelInner)

        indent(sb, indentLevelInner)
        if (visibility == JavaVisibility.PUBLIC) {
            sb.append(visibility.value)
        }

        sb.append("enum ")
        sb.append(type.shortName)

        if (superInterfaceTypes.size > 0) {
            sb.append(" implements ")

            var comma = false
            for (fqjt in superInterfaceTypes) {
                if (comma) {
                    sb.append(", ")
                } else {
                    comma = true
                }

                sb.append(JavaDomUtils.calculateTypeName(compilationUnit, fqjt))
            }
        }

        sb.append(" {")
        newLine(sb)
        indentLevelInner++

        val strIter = enumConstants.iterator()
        while (strIter.hasNext()) {
            newLine(sb)

            val enumConstant = strIter.next()
            sb.append(enumConstant.getFormattedContent(indentLevelInner))

            if (strIter.hasNext()) {
                sb.append(',')
            } else {
                sb.append(';')
            }
        }

        if (fields.size > 0) {
            newLine(sb)
        }

        val fldIter = fields.iterator()
        while (fldIter.hasNext()) {
            newLine(sb)
            val field = fldIter.next()
            sb.append(field.getFormattedContent(indentLevelInner, compilationUnit))
            if (fldIter.hasNext()) {
                newLine(sb)
            }
        }

        if (methods.size > 0) {
            newLine(sb)
        }

        val mtdIter = methods.iterator()
        while (mtdIter.hasNext()) {
            newLine(sb)
            val method = mtdIter.next()
            method.interfaceMethod = false
            sb.append(method.getFormattedContent(indentLevelInner, compilationUnit))
            if (mtdIter.hasNext()) {
                newLine(sb)
            }
        }

        if (innerClasses.size > 0) {
            newLine(sb)
        }

        val icIter = innerClasses.iterator()
        while (icIter.hasNext()) {
            newLine(sb)
            val innerClass = icIter.next()
            sb.append(innerClass.getFormattedContent(indentLevelInner, compilationUnit))
            if (icIter.hasNext()) {
                newLine(sb)
            }
        }

        if (innerInterfaces.size > 0) {
            newLine(sb)
        }
        val iiIter = innerInterfaces.iterator()
        while (iiIter.hasNext()) {
            newLine(sb)
            val innerInterface = iiIter.next()
            sb.append(innerInterface.getFormattedContent(indentLevelInner, compilationUnit))
            if (iiIter.hasNext()) {
                newLine(sb)
            }
        }

        if (innerEnums.size > 0) {
            newLine(sb)
        }

        val ieIter = innerEnums.iterator()
        while (ieIter.hasNext()) {
            newLine(sb)
            val innerEnum = ieIter.next()
            sb.append(innerEnum.getFormattedContent(indentLevelInner, compilationUnit))
            if (ieIter.hasNext()) {
                newLine(sb)
            }
        }

        indentLevelInner--
        newLine(sb)
        indent(sb, indentLevelInner)
        sb.append('}')
        newLine(sb)

        return sb.toString()
    }


    /**
     * Adds the inner class.
     *
     * @param innerClass
     * the inner class
     */
    fun innerClass(vararg innerClass: InnerClass) {
        innerClasses.addAll(innerClass)
    }

    fun innerInterface(vararg innerInterface: InnerInterface) {
        innerInterfaces.addAll(innerInterface)
    }

    /**
     * Adds the inner enum.
     *
     * @param innerEnum
     * the inner enum
     */
    fun innerEnum(vararg innerEnum: InnerEnum) {
        innerEnums.addAll(innerEnum)
    }

    /**
     * Adds the enum constant.
     *
     * @param enumConstant
     * the enum constant
     */
    private fun enumConstant(vararg enumConstant: EnumField) {
        enumConstants.addAll(enumConstant)
    }

}
