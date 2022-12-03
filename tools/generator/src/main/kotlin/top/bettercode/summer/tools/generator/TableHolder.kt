package top.bettercode.summer.tools.generator

import top.bettercode.summer.tools.generator.database.entity.Table

/**
 *
 * @author Peter Wu
 */
interface TableHolder {

    fun tables(checkFound: Boolean = true, vararg tableName: String): List<Table>

    fun tableNames(): List<String>
}