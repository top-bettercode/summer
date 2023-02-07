package top.bettercode.summer.tools.generator

import top.bettercode.summer.tools.generator.database.entity.Table
import java.io.File

/**
 *
 * @author Peter Wu
 */
abstract class FileTableHolder(
    val ext: GeneratorExtension,
    val module: String,
    val files: List<File>
) : TableHolder {

    override fun tables(checkFound: Boolean, vararg tableName: String): List<Table> {
        val tableNames = tableName.distinct()
        val all = tableNames.isEmpty()
        val result = mutableListOf<Table>()
        files.forEach { file ->
            if (all) {
                result.addAll(getTables(file).filter { !ext.excludeTableNames.contains(it.tableName) })
            } else if (result.size < tableNames.size) {
                result.addAll(getTables(file).filter { !ext.excludeTableNames.contains(it.tableName) }
                    .filter { table -> tableNames.contains(table.tableName) })

                if (result.size == tableNames.size) {
                    return result
                }
            }
        }
        if (checkFound) {
            val notFound = tableNames.filter { name -> result.none { it.tableName == name } }
            if (notFound.isNotEmpty()) {
                System.err.println("未找到${notFound}表")
            }
        }
        return result
    }

    override fun tableNames(): List<String> {
        return files.map { file ->
            getTables(file).map { it.tableName }
        }.flatten().sortedBy { it }
    }

    abstract fun getTables(
        file: File, call: (Table) -> Unit = {
            it.ext = ext
            it.module = module
        }
    ): List<Table>
}