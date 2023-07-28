package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.tools.generator.dom.java.JavaType

/**
 * @author Peter Wu
 */
open class MybatisWherePrint : ProjectGenerator() {

    override fun call() {
        columns.forEach {
            println(
                    """    <if test="${it.javaName} != null${if (it.javaType == JavaType.stringInstance) " and ${it.javaName} != ''" else ""}">
        and t.${it.columnName} ${if (it.javaType == JavaType.stringInstance) "like ${if (datasource.isOracle) "'%' || '\${${it.javaName}}' || '%'" else "concat('%', #{${it.javaName}}, '%')"}" else "= #{${it.javaName}}"}
    </if>"""
            )
        }
    }
}