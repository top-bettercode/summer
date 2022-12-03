package top.bettercode.summer.tools.generator.ddl

import top.bettercode.summer.tools.generator.GeneratorExtension
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

    fun toDDL(tables: List<Table>, out: FileUnit)

    fun toDDLUpdate(
        module: String, oldTables: List<Table>, tables: List<Table>,
        out: Writer,
        extension: GeneratorExtension
    )

    fun appendTable(table: Table, pw: Writer)
}