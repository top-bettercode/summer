package top.bettercode.summer.tools.lang.log

/**
 *
 * @author Peter Wu
 */
class SqlLogData {
    var sql: String? = null
    var params: MutableList<SqlLogParam> = mutableListOf()
    var cost: Long? = null
    var affected: Int? = null
    var retrieved: Int? = null
    var total: Long? = null
}