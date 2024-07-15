package top.bettercode.summer.tools.lang.log

import top.bettercode.summer.tools.lang.util.JavaTypeResolver

/**
 *
 * @author Peter Wu
 */
class SqlLogData(val id: String? = null) {
    var sql: String? = null
    var slowSql: MutableList<String> = mutableListOf()
    var params: MutableList<SqlLogParam> = mutableListOf()
    var cost: Long? = null
    var affected: Int? = null
    var retrieved: Int? = null
    var total: Long? = null
    var offset: Long? = null
    var limit: Int? = null
    var error: String? = null

    fun addParams(args: Array<Any?>, types: IntArray) {
        args.forEachIndexed { index, any ->
            params.add(
                SqlLogParam(index, JavaTypeResolver.type(types[index])?.javaType, any.toString())
            )
        }
    }

    override fun toString(): String {
        var sql = sql?.trim()
        if (!sql.isNullOrBlank() && params.isNotEmpty()) {
            val params = params.sortedBy { it.index }
            for (i in params.indices) {
                sql = sql!!.replaceFirst("?", params[i].toString())
            }
            //limit ? offset ?
            if (sql!!.contains("limit ?") && limit != null) {
                sql = sql.replaceFirst("limit ?", "limit #${limit}")
            }
            if (sql.contains("offset ?") && offset != null) {
                sql = sql.replaceFirst("offset ?", "offset #${offset}")
            }
            //where
            //            rownum <= ?
            //        )
            //    where
            //        rownum_ > ?
            if (sql.contains("rownum <= ?") && offset != null && limit != null) {
                sql = sql.replaceFirst("rownum <= ?", "rownum <= #${offset!! + limit!!}")
            }
            if (sql.contains("rownum_ > ?") && offset != null) {
                sql = sql.replaceFirst("rownum_ > ?", "rownum_ > #${offset!!}")
            }

            //fetch next ? rows only;
            if (sql.contains("fetch next ?") && limit != null) {
                sql = sql.replaceFirst("fetch next ?", "fetch next #${limit}")
            }

        }
        val resultInfo = "${
            if (affected != null) "affected:${affected} rows; " else ""
        }${
            if (retrieved != null) "retrieved:${retrieved} rows; " else ""
        }${
            if (total != null) "total:${total} rows; " else ""
        }${
            if (cost != null) "cost:${cost} ms;" else ""
        }"
        return "${if (id.isNullOrBlank()) "" else "${id}: "}$resultInfo${if (sql.isNullOrBlank()) "" else "\n$sql"} ${if (error.isNullOrBlank()) "" else "\n$error"}"
    }
}