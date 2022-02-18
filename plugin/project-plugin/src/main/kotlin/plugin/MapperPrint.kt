package plugin

import ProjectGenerator

/**
 * @author Peter Wu
 */
open class MapperPrint : ProjectGenerator() {

    override fun call() {
        println("""<resultMap type="${entityType.fullyQualifiedNameWithoutTypeParameters}" id="${entityName}Map">""")
        columns.forEach {
            println("    <result property=\"${it.javaName}\" column=\"${it.columnName}\"/>")
        }
        println("""  </resultMap>""")
    }
}