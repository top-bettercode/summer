package top.bettercode.summer.tools.generator.ddl

import top.bettercode.summer.tools.generator.DatabaseConfiguration
import top.bettercode.summer.tools.generator.database.entity.Table
import top.bettercode.summer.tools.generator.dom.unit.FileUnit
import java.io.Writer

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

    fun toDDL(tables: List<Table>, out: FileUnit, database: DatabaseConfiguration)

    fun toDDLUpdate(oldTables: List<Table>, tables: List<Table>, out: Writer, database: DatabaseConfiguration)

    fun appendTable(prefixTableName: String, table: Table, pw: Writer, database: DatabaseConfiguration)
}