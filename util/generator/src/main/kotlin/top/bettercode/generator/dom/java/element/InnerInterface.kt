package top.bettercode.generator.dom.java.element

import top.bettercode.generator.dom.java.JavaDomUtils
import top.bettercode.generator.dom.java.JavaType

/**
 * The Class Interface.
 *
 */
open class InnerInterface(type: JavaType) : InnerUnit(type) {
    /** The inner interfaces.  */
    protected val innerInterfaces: MutableList<InnerInterface> = mutableListOf()

    // interfaces do not have superclasses
    val superClass: JavaType?
        get() = null

    val isJavaInterface: Boolean
        get() = true

    val isJavaEnumeration: Boolean
        get() = false


    /**
     * Adds the inner interface.
     *
     * @param innerInterface
     * the inner interface
     */
    fun innerInterface(vararg innerInterface: InnerInterface) {
        innerInterfaces.addAll(innerInterface)
    }

    override fun calculateImports(importedTypes: MutableSet<JavaType>): Set<String> {
        innerInterfaces.forEach {
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
        sb.append(visibility.value)

        if (isStatic) {
            sb.append("static ")
        }

        if (isFinal) {
            sb.append("final ")
        }

        sb.append("interface ")
        sb.append(type.shortName)

        if (superInterfaceTypes.isNotEmpty()) {
            sb.append(" extends ")

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

        val fldIter = fields.iterator()
        while (fldIter.hasNext()) {
            newLine(sb)
            val field = fldIter.next()
            sb.append(field.getFormattedContent(indentLevelInner, compilationUnit))
        }

        if (fields.size > 0 && methods.size > 0) {
            newLine(sb)
        }

        val mtdIter = methods.iterator()
        while (mtdIter.hasNext()) {
            newLine(sb)
            val method = mtdIter.next()
            method.interfaceMethod = true
            sb.append(method.getFormattedContent(indentLevelInner, compilationUnit))
            if (mtdIter.hasNext()) {
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

        indentLevelInner--
        newLine(sb)
        indent(sb, indentLevelInner)
        sb.append('}')

        return sb.toString()
    }

}
