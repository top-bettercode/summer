package plugin

import ProjectGenerator

/**
 * @author Peter Wu
 */
open class MapperPrint : ProjectGenerator() {

    override fun call() {
        println("""<resultMap type="${entityType.fullyQualifiedNameWithoutTypeParameters}" id="${entityName}Map">""")
        if (isCompositePrimaryKey) {
            primaryKeys.forEach {
                println("""    <result property="${primaryKeyName}.${it.javaName}" column="${it.columnName}" />""")
            }
        } else {
            println("    <result property=\"${primaryKeyName}\" column=\"${primaryKey.columnName}\"/>")
        }
        otherColumns.forEach {
            println("    <result property=\"${it.javaName}\" column=\"${it.columnName}\"/>")
        }
        println("""</resultMap>""")
    }
}