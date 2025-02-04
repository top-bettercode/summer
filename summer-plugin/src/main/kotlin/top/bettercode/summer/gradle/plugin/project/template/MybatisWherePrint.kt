package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.tools.lang.util.JavaType

/**
 * @author Peter Wu
 */
open class MybatisWherePrint : ProjectGenerator() {

    override fun call() {
        columns.forEach {
            println(
                    """    <if test="${it.javaName} != null${if (it.javaType == JavaType.stringInstance) " and ${it.javaName} != ''" else ""}">
        and t.${it.columnName} ${if (it.javaType == JavaType.stringInstance) "like ${if (database.isOracle) "'%' || '\${${it.javaName}}' || '%'" else "concat('%', #{${it.javaName}}, '%')"}" else "= #{${it.javaName}}"}
    </if>"""
            )
        }
    }
}