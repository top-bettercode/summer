package top.bettercode.generator.dom.java.element

import top.bettercode.generator.dom.java.StringOperator

class EnumField(val string: String) {
    /** The java doc lines.  */
    private val javaDocLines: MutableList<String> = mutableListOf()

    fun javadoc(javadoc: StringOperator.() -> Unit) {
        javadoc(StringOperator(javaDocLines))
    }

    fun getFormattedContent(indentLevel: Int): String {
        val sb = StringBuilder()

        for (javaDocLine in javaDocLines) {
            JavaElement.indent(sb, indentLevel)
            sb.append(javaDocLine)
            JavaElement.newLine(sb)
        }
        JavaElement.indent(sb, indentLevel)
        sb.append(string)
        return sb.toString()
    }
}
