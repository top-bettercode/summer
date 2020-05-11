package cn.bestwu.generator.ddl

import cn.bestwu.generator.database.entity.Table
import java.io.File
import java.io.PrintWriter

/**
 *
 * @author Peter Wu
 */
interface IToDDL {

    /**
     * 是否使用引号
     */
    var useQuote: Boolean
    /**
     * 生成SQL时是否生成外键相关语句
     */
    var useForeignKey: Boolean
    /**
     * 引号
     */
    val quoteMark: String
    /**
     * 注释前缀
     */
    val commentPrefix: String

    fun toDDL(tables: List<Table>, out: File)

    fun toDDLUpdate(oldTables: List<Table>, tables: List<Table>, out: PrintWriter, deleteTablesWhenUpdate: Boolean = false)

    fun appendTable(table: Table, pw: PrintWriter)
}