package top.bettercode.generator

import top.bettercode.generator.database.entity.Table
import java.io.File

/**
 *
 * @author Peter Wu
 */
abstract class FileTableHolder(val module: String, val files: List<File>) : TableHolder {

    override fun tables(vararg tableName: String): List<Table> {
        val tableNames = tableName.distinct()
        val all = tableNames.isEmpty()
        val result = mutableListOf<Table>()
        files.forEach { file ->
            if (all) {
                result.addAll(getTables(file))
            } else if (result.size < tableNames.size) {
                result.addAll(getTables(file).filter { table -> tableNames.contains(table.tableName) })

                if (result.size == tableNames.size) {
                    return result
                }
            }
        }
        val notFound = tableNames.filter { name -> result.none { it.tableName == name } }
        if (notFound.isNotEmpty()) {
            System.err.println("未找到${notFound}表")
        }
        return result
    }

    override fun tableNames(): List<String> {
        return files.map { file ->
            getTables(file).map { it.tableName }
        }.flatten().sortedBy { it }
    }

    abstract fun getTables(file: File): List<Table>
}