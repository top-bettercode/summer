package top.bettercode.summer.tools.lang.log

import top.bettercode.summer.tools.lang.util.JavaTypeResolver

/**
 *
 * @author Peter Wu
 */
class SqlLogData(val id: String? = null) {
    var sql: String? = null
        set(value) {
            field = value
            paramCount = value?.count { it == '?' } ?: 0
        }
    var paramCount: Int = 0
    var slowSql: MutableList<String> = mutableListOf()
    var params: MutableList<SqlLogParam> = mutableListOf()
    var cost: Long? = null
    var affected: Any? = null
    var retrieved: Int? = null
    var total: Long? = null
    var result: String? = null
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
        if (!sql.isNullOrBlank()) {
            if (params.isNotEmpty()) {
                val params = params.sortedBy { it.index }
                for (i in params.indices) {
                    sql = sql!!.replaceFirst("?", params[i].toString())
                }
            }

            //limit ? offset ?
            val qsql = sql!!.lowercase().replace("\\s+".toRegex(), " ")
                .replace(Regex("\\u001B\\[[;\\d]*m"), "")
            if (qsql.contains("limit ? offset ?") && limit != null && offset != null) {
                sql = sql.replaceFirst("?", "#$limit")
                sql = sql.replaceFirst("?", "#$offset")
            } else if (qsql.contains("limit ?, ?") && limit != null && offset != null) {
                sql = sql.replaceFirst("?", "#$offset")
                sql = sql.replaceFirst("?", "#$limit")
            }
            if (qsql.contains("limit ?") && limit != null) {
                sql = sql.replaceFirst("?", "#$limit")
            }
            if (qsql.contains("offset ?") && offset != null) {
                sql = sql.replaceFirst("?", "#$offset")
            }
            //where
            //            rownum <= ?
            //        )
            //    where
            //        rownum_ > ?
            if (qsql.contains("rownum <= ?") && offset != null && limit != null) {
                sql = sql.replaceFirst("?", "#${offset!! + limit!!}")
            }
            if (qsql.contains("rownum_ > ?") && offset != null) {
                sql = sql.replaceFirst("?", "#${offset}")
            }
            //fetch next ? rows only;
            if (qsql.contains("fetch next ?") && limit != null) {
                sql = sql.replaceFirst("?", "#${limit}")
            }
            //fetch first ? rows only
            if (qsql.contains("fetch first ?") && limit != null) {
                sql = sql.replaceFirst("?", "#${limit}")
            }
        }

        val resultInfo = "${
            if (affected != null) "affected:${affected} rows; " else ""
        }${
            if (retrieved != null) "retrieved:${retrieved} rows; " else ""
        }${
            if (total != null) "total:${total} rows; " else ""
        }${
            if (result != null) "result:${result}; " else ""
        }${
            if (cost != null) "cost:${cost} ms;" else ""
        }"
        return "${if (id.isNullOrBlank()) "" else "${id}: "}$resultInfo${if (sql.isNullOrBlank()) "" else "\n$sql"} ${if (error.isNullOrBlank()) "" else "\nERROR:$error"}"
    }
}