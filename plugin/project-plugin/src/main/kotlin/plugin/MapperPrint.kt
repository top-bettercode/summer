package plugin

import ModuleJavaGenerator

/**
 * @author Peter Wu
 */
open class MapperPrint : ModuleJavaGenerator() {

    override fun content() {
    }

    override fun doCall() {
        println("""<resultMap type="${entityType.fullyQualifiedNameWithoutTypeParameters}" id="${entityName}Map">""")
        columns.forEach {
            println("    <result property=\"${it.javaName}\" column=\"${it.columnName}\"/>")
        }
        println("""  </resultMap>""")
    }
}