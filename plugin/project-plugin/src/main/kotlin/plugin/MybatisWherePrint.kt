package plugin

import ModuleJavaGenerator
import cn.bestwu.generator.dom.java.JavaType

/**
 * @author Peter Wu
 */
open class MybatisWherePrint : ModuleJavaGenerator() {

    override fun content() {
    }

    override fun doCall() {

        columns.forEach {
            println("""    <if test="${it.javaName} != null${if (it.javaType == JavaType.stringInstance) " and ${it.javaName} != ''" else ""}">
        and t.${it.columnName} ${if (it.javaType == JavaType.stringInstance) "like ${if (extension.datasource.isOracle) "'%' || '\${${it.javaName}}' || '%'" else "concat('%', #{${it.javaName}}, '%')"}" else "= #{${it.javaName}}"}
    </if>""")
        }
    }
}