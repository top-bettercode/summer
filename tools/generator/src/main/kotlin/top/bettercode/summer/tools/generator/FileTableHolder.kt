package top.bettercode.summer.tools.generator

import org.joni.Config.log
import top.bettercode.summer.tools.generator.database.entity.Table
import java.io.File

/**
 *
 * @author Peter Wu
 */
abstract class FileTableHolder(
        val database: DatabaseConfiguration,
        val files: List<File>
) : TableHolder {

    private val log = org.slf4j.LoggerFactory.getLogger(FileTableHolder::class.java)

    override fun tables(checkFound: Boolean, vararg tableName: String): List<Table> {
        val tableNames = tableName.distinct()
        val all = tableNames.isEmpty()
        val result = mutableListOf<Table>()
        files.forEach { file ->
            if (all) {
                result.addAll(getTables(file).filter { !database.excludeTableNames.contains(it.tableName) })
            } else if (result.size < tableNames.size) {
                result.addAll(getTables(file).filter { !database.excludeTableNames.contains(it.tableName) }
                        .filter { table -> tableNames.contains(table.tableName) })

                if (result.size == tableNames.size) {
                    return result
                }
            }
        }
        if (checkFound) {
            val notFound = tableNames.filter { name -> result.none { it.tableName == name } }
            if (notFound.isNotEmpty()) {
                log.error("未找到${notFound}表")
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
                it.database = database
                it.charset = database.charset
                it.collate = database.collate
            }
    ): List<Table>
}