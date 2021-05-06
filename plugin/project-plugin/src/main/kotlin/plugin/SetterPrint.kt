package plugin

import ModuleJavaGenerator

/**
 * @author Peter Wu
 */
open class SetterPrint(private val randomValue: Boolean) : ModuleJavaGenerator() {

    override fun content() {
    }

    override fun doCall() {

        if (compositePrimaryKey || !primaryKey.autoIncrement) {
            if (compositePrimaryKey) {
                println("${primaryKeyType.shortName} $primaryKeyName = new ${primaryKeyType.shortName}();")
                primaryKeys.forEach {
                    println("$primaryKeyName.set${it.javaName.capitalize()}(${if (randomValue) it.randomValueToSet else ""});")
                }
                println("$entityName.set${primaryKeyName.capitalize()}(${primaryKeyName});")
            } else
                primaryKeys.forEach {
                    println("$entityName.set${it.javaName.capitalize()}(${if (randomValue) it.randomValueToSet else ""});")
                }
        }
        otherColumns.forEach {
            println("$entityName.set${it.javaName.capitalize()}(${if (randomValue) it.randomValueToSet else ""});")
        }
    }
}