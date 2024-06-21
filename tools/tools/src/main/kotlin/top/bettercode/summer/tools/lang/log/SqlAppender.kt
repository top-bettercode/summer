package top.bettercode.summer.tools.lang.log

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import top.bettercode.summer.tools.lang.operation.HttpOperation
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class SqlAppender : AppenderBase<ILoggingEvent>() {
    companion object {
        const val PID = "PID"
        const val MDC_SQL_ERROR = "SQL_ERROR"
        const val MDC_SQL_ID = "SQL_ID"
        const val MDC_SQL_END = "SQL_END"
    }

    private val sqlCache: ConcurrentMap<String, SqlLogData> = ConcurrentHashMap()
    private val logger = LoggerFactory.getLogger(SqlAppender::class.java)

    public override fun append(event: ILoggingEvent?) {
        if (event == null || !isStarted || !logger.isInfoEnabled) {
            return
        }
        try {
            val traceid = event.mdcPropertyMap[HttpOperation.MDC_TRACEID]
                ?: "${event.loggerContextVO.propertyMap[PID]}-${event.threadName}"
            val id = event.mdcPropertyMap[MDC_SQL_ID] ?: ""
            val error = event.mdcPropertyMap[MDC_SQL_ERROR]
            val end = !event.mdcPropertyMap[MDC_SQL_END].isNullOrBlank()
            val key = "$traceid:$id"
            var sqlLogData = sqlCache.computeIfAbsent(key) { SqlLogData() }
            val msg = event.formattedMessage
            when (event.loggerName) {
                "org.hibernate.SQL" -> {
                    if (!sqlLogData.sql.isNullOrBlank()) {
                        sqlCache.remove(key)
                        log(sqlLogData, id, error)
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
                    val regex = Regex("total: (\\d+) rows")
                    val matchResult = regex.find(msg)
                    if (matchResult != null) {
                        sqlLogData.total = matchResult.groupValues[1].toLong()
                    }
                    //{} rows retrieved
                    val regex2 = Regex("(\\d+) rows retrieved")
                    val matchResult2 = regex2.find(msg)
                    if (matchResult2 != null) {
                        sqlLogData.retrieved = matchResult2.groupValues[1].toInt()
                    }
                    //{} row affected
                    val regex3 = Regex("(\\d+) row affected")
                    val matchResult3 = regex3.find(msg)
                    if (matchResult3 != null) {
                        sqlLogData.affected = matchResult3.groupValues[1].toInt()
                    }
                    //cost: {} ms
                    val regex4 = Regex("cost: (\\d+) ms")
                    val matchResult4 = regex4.find(msg)
                    if (matchResult4 != null) {
                        sqlLogData.cost = matchResult4.groupValues[1].toLong()

                    }
                }
            }
            if (end) {
                log(sqlLogData, id, error)
                sqlCache.remove(key)
            }
        } catch (e: Exception) {
            logger.error("sql日志记录失败", e)
        }
    }

    private fun log(
        sqlLogData: SqlLogData,
        id: String,
        error: String?
    ) {
        var sql = sqlLogData.sql ?: ""
        if (sql.isNotBlank() && sqlLogData.params.isNotEmpty()) {
            val params = sqlLogData.params.sortedBy { it.index }
            for (i in params.indices) {
                sql = sql.replaceFirst("?", params[i].toString())
            }
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
        if (error.isNullOrBlank())
            logger.info(
                "{}$resultInfo {}",
                if (id.isNotBlank()) "${id}: " else "",
                sql
            )
        else
            logger.error(
                MarkerFactory.getMarker(AlarmAppender.NO_ALARM_LOG_MARKER),
                "{}$resultInfo\n{} {}",
                if (id.isNotBlank()) "${id}: " else "",
                sql,
                error
            )
    }

}