package plugin

import ProjectGenerator
import java.util.*

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
                            it.javaName.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            }
                        }(${if (randomValue) it.randomValueToSet else ""});")
                }
                println(
                    "$entityName.set${
                        primaryKeyName.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.getDefault()
                            ) else it.toString()
                        }
                    }(${primaryKeyName});")
            } else
                primaryKeys.forEach {
                    println(
                        "$entityName.set${
                            it.javaName.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            }
                        }(${if (randomValue) it.randomValueToSet else ""});")
                }
        }
        otherColumns.forEach {
            println(
                "$entityName.set${
                    it.javaName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                }(${if (randomValue) it.randomValueToSet else ""});")
        }
    }
}