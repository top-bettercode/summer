package top.bettercode.summer.tools.lang.log

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class SqlAppender : AppenderBase<ILoggingEvent>() {

    private val sqlCache: ConcurrentMap<String, SqlLogData> = ConcurrentHashMap()
    private val logger = LoggerFactory.getLogger(SqlAppender::class.java)

    public override fun append(event: ILoggingEvent?) {
        if (event == null || !isStarted || !logger.isDebugEnabled) {
            return
        }
        val pid = event.loggerContextVO.propertyMap["PID"]
        val id = event.mdcPropertyMap["id"] ?: ""
        val key = "$pid:$id"
        var sqlLogData = sqlCache.computeIfAbsent(key) { SqlLogData() }
        val msg = event.formattedMessage
        when (event.loggerName) {
            "org.hibernate.SQL" -> {
                if (!sqlLogData.sql.isNullOrBlank()) {
                    sqlCache.remove(key)
                    log(sqlLogData, id)
                    sqlLogData = sqlCache.computeIfAbsent(key) { SqlLogData() }
                }
                sqlLogData.sql = msg
            }

            "org.hibernate.type.descriptor.sql.BasicBinder" -> {
                val regex = Regex("""\[(.*?)]""")
                val matches = regex.findAll(msg).map { it.groupValues[1] }.toList()
                val index = matches[0].toInt()
                sqlLogData.params.add(SqlLogParam(index, matches[1], matches[2]))
            }

            else -> {
                //total: {} rows
                val regex = Regex("total: (\\d)+ rows")
                val matchResult = regex.find(msg)
                if (matchResult != null) {
                    sqlLogData.total = matchResult.groupValues[1].toLong()
                }
                //{} rows retrieved
                val regex2 = Regex("(\\d)+ rows retrieved")
                val matchResult2 = regex2.find(msg)
                if (matchResult2 != null) {
                    sqlLogData.retrieved = matchResult2.groupValues[1].toInt()
                }
                //{} row affected
                val regex3 = Regex("(\\d)+ row affected")
                val matchResult3 = regex3.find(msg)
                if (matchResult3 != null) {
                    sqlLogData.affected = matchResult3.groupValues[1].toInt()
                }
                //cost: {} ms
                val regex4 = Regex("cost: (\\d)+ ms")
                val matchResult4 = regex4.find(msg)
                if (matchResult4 != null) {
                    sqlLogData.cost = matchResult4.groupValues[1].toLong()
                    log(sqlLogData, id)
                    sqlCache.remove(key)
                }
            }
        }
    }

    private fun log(
        sqlLogData: SqlLogData,
        id: String
    ) {
        var sql = sqlLogData.sql!!
        val params = sqlLogData.params.sortedBy { it.index }
        for (i in params.indices) {
            sql = sql.replaceFirst("?", params[i].toString())
        }
        val resultInfo = "${
            if (sqlLogData.affected != null) "affected:${sqlLogData.affected} rows; " else ""
        }${
            if (sqlLogData.retrieved != null) "retrieved:${sqlLogData.retrieved} rows; " else ""
        }${
            if (sqlLogData.total != null) "total:${sqlLogData.total} rows; " else ""
        }${
            if (sqlLogData.cost != null) "cost:${sqlLogData.cost} ms;" else ""
        }".trim()
        logger.debug(
            "{}{}\n$resultInfo",
            if (id.isNotBlank()) "${id}: " else "",
            sql
        )
    }

}