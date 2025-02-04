package top.bettercode.summer.tools.lang.log

import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

/**
 *
 * @author Peter Wu
 */
class SqlLogData(val id: String? = null) {
    var start: Long = System.currentTimeMillis()
    var end: Long? = null
    var sql: String? = null
        set(value) {
            field = value
            paramCount = value?.count { it == '?' } ?: 0
        }
    var paramCount: Int = 0
    var slowSql: MutableList<String> = mutableListOf()
    var params: MultiValueMap<Int, SqlLogParam> = LinkedMultiValueMap()
    var affected: Any? = null
    var retrieved: Int? = null
    var total: Long? = null
    var result: String? = null
    var offset: Long? = null
    var limit: Int? = null
    var error: String? = null
    var ignoreTimeout: Boolean = false

    val cost: Long?
        get() {
            return if (sql.isNullOrBlank() && slowSql.isNotEmpty()) {
                // "SlowQuery: " + queryExecutionMillis + " milliseconds. SQL: '" + sql + "'";
                slowSql[0].trim().substringAfter("SlowQuery: ")
                    .substringBefore(" milliseconds. SQL: '")
                    .trim().toLong()
            } else end?.minus(start)
        }

    fun toSql(): String {
        var part = 0
        val originSql =
            sql?.trim() ?: (if (slowSql.isNotEmpty()) slowSql.joinToString("\n------\n") else null)
        val cost = this.cost

        val sqlParams = mutableListOf<String>()
        var sql: String?
        if (originSql != null && paramCount > 0) {
            if (params.isNotEmpty()) {
                val keys = params.keys.sorted()
                for (i in keys) {
                    if (part < paramCount) {
                        sqlParams.add(params[i]?.firstOrNull()?.toString() ?: "#null")
                        part++
                    }
                }
            }

            //limit ? offset ?
            val qsql = originSql.lowercase().replace("\\s+".toRegex(), " ")
                .replace(Regex("\\u001B\\[[;\\d]*m"), "")
            if (qsql.contains("limit ? offset ?") && limit != null && offset != null) {
                if (part < paramCount) {
                    sqlParams.add("#$limit")
                    part++
                }
                if (part < paramCount) {
                    sqlParams.add("#$offset")
                    part++
                }
            } else if (qsql.contains("limit ?, ?") && limit != null && offset != null) {
                if (part < paramCount) {
                    sqlParams.add("#$offset")
                    part++
                }
                if (part < paramCount) {
                    sqlParams.add("#$limit")
                    part++
                }
            }
            if (qsql.contains("limit ?") && limit != null) {
                if (part < paramCount) {
                    sqlParams.add("#$limit")
                    part++
                }
            }
            if (qsql.contains("offset ?") && offset != null) {
                if (part < paramCount) {
                    sqlParams.add("#$offset")
                    part++
                }
            }
            //where
            //            rownum <= ?
            //        )
            //    where
            //        rownum_ > ?
            if (qsql.contains("rownum <= ?") && offset != null && limit != null) {
                if (part < paramCount) {
                    sqlParams.add("#${offset!! + limit!!}")
                    part++
                }
            }
            if (qsql.contains("rownum_ > ?") && offset != null) {
                if (part < paramCount) {
                    sqlParams.add("#$offset")
                    part++
                }
            }
            //fetch next ? rows only;
            if (qsql.contains("fetch next ?") && limit != null) {
                if (part < paramCount) {
                    sqlParams.add("#$limit")
                    part++
                }
            }
            //fetch first ? rows only
            if (qsql.contains("fetch first ?") && limit != null) {
                if (part < paramCount) {
                    sqlParams.add("#$limit")
                }
            }
            sql = ""
            val strings = originSql.split("?")
            strings.forEachIndexed { index, s ->
                sql += if (index < sqlParams.size) {
                    s + sqlParams[index]
                } else {
                    s
                }
            }
        } else {
            sql = originSql
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
            if (cost != null && cost > 0) "cost:${cost} ms;" else ""
        }"
        return "${if (id.isNullOrBlank()) "" else "${id}: "}$resultInfo${if (sql.isNullOrBlank()) "" else "\n$sql"} ${if (error.isNullOrBlank()) "" else "\nERROR:$error"}"
    }

    override fun toString(): String {
        return toSql()
    }


}