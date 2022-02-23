package plugin

import ProjectGenerator

/**
 * @author Peter Wu
 */
open class SetterPrint(private val randomValue: Boolean) : ProjectGenerator() {

    override fun call() {
        if (isCompositePrimaryKey || !primaryKey.autoIncrement) {
            if (isCompositePrimaryKey) {
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