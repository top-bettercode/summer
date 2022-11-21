package plugin

import jpa.ProjectGenerator
import top.bettercode.lang.capitalized

/**
 * @author Peter Wu
 */
open class SetterPrint(private val randomValue: Boolean) : ProjectGenerator() {

    override fun call() {
        if (isCompositePrimaryKey || !primaryKey.autoIncrement) {
            if (isCompositePrimaryKey) {
                println("${primaryKeyType.shortName} $primaryKeyName = new ${primaryKeyType.shortName}();")
                primaryKeys.forEach {
                    println(
                        "$primaryKeyName.set${
                            it.javaName.capitalized()
                        }(${if (randomValue) it.randomValueToSet() else ""});"
                    )
                }
                println(
                    "$entityName.set${
                        primaryKeyName.capitalized()
                    }(${primaryKeyName});"
                )
            } else
                primaryKeys.forEach {
                    println(
                        "$entityName.set${
                            it.javaName.capitalized()
                        }(${if (randomValue) it.randomValueToSet() else ""});"
                    )
                }
        }
        otherColumns.filter { !it.version }.forEach {
            println(
                "$entityName.set${
                    it.javaName.capitalized()
                }(${if (randomValue) it.randomValueToSet() else ""});"
            )
        }
    }
}