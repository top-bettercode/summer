package top.bettercode.summer.tools.generator.dom.java.element

import top.bettercode.summer.tools.generator.dom.java.JavaDomUtils
import top.bettercode.summer.tools.generator.dom.java.JavaType

class TypeParameter @JvmOverloads constructor(
    /**
     * @return Returns the name.
     */
    val name: String,
    /**
     * @return Returns the extends types.
     */
    val extendsTypes: List<JavaType> = ArrayList()
) {

    fun getFormattedContent(compilationUnit: CompilationUnit?): String {
        val sb = StringBuilder()

        sb.append(name)
        if (extendsTypes.isNotEmpty()) {

            sb.append(" extends ")
            var addAnd = false
            for (type in extendsTypes) {
                if (addAnd) {
                    sb.append(" & ")
                } else {
                    addAnd = true
                }
                sb.append(JavaDomUtils.calculateTypeName(compilationUnit, type))
            }
        }

        return sb.toString()
    }

    override fun toString(): String {
        return getFormattedContent(null)
    }
}
