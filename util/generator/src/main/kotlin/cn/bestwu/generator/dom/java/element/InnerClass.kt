package cn.bestwu.generator.dom.java.element

import cn.bestwu.generator.dom.java.JavaDomUtils
import cn.bestwu.generator.dom.java.JavaType

/**
 * This class encapsulates the idea of an inner class - it has methods that make
 * it easy to generate inner classes.
 */
open class InnerClass(type: JavaType) : InnerUnit(type) {


    /** The inner classes.  */
    private val innerClasses: MutableList<InnerClass> = mutableListOf()

    /** The inner enums.  */
    private val innerEnums: MutableList<InnerEnum> = mutableListOf()

    /** The type parameters.  */
    val typeParameters: MutableList<TypeParameter> = mutableListOf()

    /**
     * Sets the super class.
     *
     * The superClass to set.
     */
    var superClass: JavaType? = null

    /** The is abstract.  */
    /**
     * Sets the abstract.
     *
     * the new abstract
     */
    var isAbstract: Boolean = false

    /** The initialization blocks.  */
    private val initializationBlocks: MutableList<InitializationBlock> = mutableListOf()

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

        if (isAbstract) {
            sb.append("abstract ")
        }

        if (isStatic) {
            sb.append("static ")
        }

        if (isFinal) {
            sb.append("final ")
        }

        sb.append("class ")
        sb.append(type.shortName)

        if (this.typeParameters.isNotEmpty()) {
            var comma = false
            sb.append("<")
            for (typeParameter in typeParameters) {
                if (comma) {
                    sb.append(", ")
                }
                sb.append(typeParameter.getFormattedContent(compilationUnit))
                comma = true
            }
            sb.append("> ")
        }

        if (superClass != null) {
            sb.append(" extends ")
            sb.append(JavaDomUtils.calculateTypeName(compilationUnit, superClass!!))
        }

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

        val fldIter = fields.iterator()
        while (fldIter.hasNext()) {
            newLine(sb)
            val field = fldIter.next()
            sb.append(field.getFormattedContent(indentLevelInner, compilationUnit))
            if (fldIter.hasNext()) {
                newLine(sb)
            }
        }

        if (initializationBlocks.size > 0) {
            newLine(sb)
        }

        val blkIter = initializationBlocks.iterator()
        while (blkIter.hasNext()) {
            newLine(sb)
            val initializationBlock = blkIter.next()
            sb.append(initializationBlock.getFormattedContent(indentLevelInner))
            if (blkIter.hasNext()) {
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

        return sb.toString()
    }


    /**
     * Sets the super class.
     *
     * @param superClassType
     * the new super class
     */
    fun superClass(superClassType: String) {
        this.superClass = JavaType(superClassType)
    }

    /**
     * Sets the super class.
     *
     * @param superClassType
     * the new super class
     */
    fun superClass(superClassType: JavaType) {
        this.superClass = superClassType
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
     * Adds the type parameter.
     *
     * @param typeParameter
     * the type parameter
     */
    fun typeParameter(vararg typeParameter: TypeParameter) {
        this.typeParameters.addAll(typeParameter)
    }

    /**
     * Adds the initialization block.
     *
     * @param initializationBlock
     * the initialization block
     */
    fun init(vararg initializationBlock: InitializationBlock) {
        initializationBlocks.addAll(initializationBlock)
    }

    fun init(block: InitializationBlock.() -> Unit) {
        val initializationBlock = InitializationBlock(false)
        block(initializationBlock)
        initializationBlocks.add(initializationBlock)
    }

    fun static(block: InitializationBlock.() -> Unit) {
        val initializationBlock = InitializationBlock(true)
        block(initializationBlock)
        initializationBlocks.add(initializationBlock)
    }
}
