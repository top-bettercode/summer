package top.bettercode.generator

import top.bettercode.generator.database.entity.Table

/**
 *
 * @author Peter Wu
 */
interface TableHolder {

    fun tables(vararg tableName: String): List<Table>

    fun tableNames(): List<String>
}