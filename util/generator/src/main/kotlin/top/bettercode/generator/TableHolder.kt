package top.bettercode.generator

import top.bettercode.generator.database.entity.Table

/**
 *
 * @author Peter Wu
 */
interface TableHolder {

    var tablePrefixes: Array<String>

    fun tables(checkFound: Boolean = true, vararg tableName: String): List<Table>

    fun tableNames(): List<String>
}