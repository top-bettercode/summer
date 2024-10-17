package top.bettercode.summer.tools.lang.log

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.slf4j.ILoggerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.util.Assert
import top.bettercode.summer.tools.lang.operation.HttpOperation
import top.bettercode.summer.tools.lang.util.JavaTypeResolver
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class SqlAppender(private val timeoutAlarmMS: Long) : AppenderBase<ILoggingEvent>() {
    companion object {
        const val MDC_SQL_ERROR = "SQL_ERROR"
        const val MDC_SQL_ID = "SQL_ID"
        const val MDC_SQL_END = "SQL_END"
        const val MDC_SQL_TOTAL = "SQL_TOTAL"
        const val MDC_SQL_RESULT = "SQL_RESULT"
        const val MDC_SQL_RETRIEVED = "SQL_RETRIEVED"
        const val MDC_SQL_AFFECTED = "SQL_AFFECTED"
        const val MDC_SQL_TIME_START = "SQL_TIME_START"
        const val MDC_SQL_TIME_END = "SQL_TIME_END"
        const val MDC_SQL_OFFSET = "SQL_OFFSET"
        const val MDC_SQL_LIMIT = "SQL_LIMIT"
        const val LOG_SLOW = "org.hibernate.SQL_SLOW"

        val loggerContext: LoggerContext by lazy {
            val factory = LoggerFactory.getILoggerFactory()
            Assert.isInstanceOf(
                LoggerContext::class.java, factory,
                String.format(
                    "LoggerFactory is not a Logback LoggerContext but Logback is on "
                            + "the classpath. Either remove Logback or the competing "
                            + "implementation (%s loaded from %s). If you are using "
                            + "WebLogic you will need to add 'org.slf4j' to "
                            + "prefer-application-packages in WEB-INF/weblogic.xml",
                    factory.javaClass, getLocation(factory)
                )
            )
            factory as LoggerContext
        }

        private fun getLocation(factory: ILoggerFactory): Any {
            try {
                val protectionDomain = factory.javaClass.protectionDomain
                val codeSource = protectionDomain.codeSource
                if (codeSource != null) {
                    return codeSource.location
                }
            } catch (ex: SecurityException) {
                // Unable to determine location
            }

            return "unknown location"
        }

        fun isShowSql(): Boolean {
            return loggerContext.getLogger("org.hibernate.SQL").level == Level.DEBUG
        }

        fun setSqlLevel(
            level: Level,
            defaultLevel: Level = loggerContext.getLogger("ROOT").level
        ): Level? {
            val showSql = Level.DEBUG == level
            val sqlLogger = loggerContext.getLogger("org.hibernate.SQL")
            val lastLevel = sqlLogger.level
            sqlLogger.level = level
            loggerContext.getLogger("top.bettercode.summer.SQL").level = level
            loggerContext.getLogger("org.hibernate.type.descriptor.sql.BasicBinder").level =
                if (showSql) Level.TRACE else defaultLevel
            return lastLevel
        }

        @JvmStatic
        fun disableLog(runnable: Runnable) {
            if (isShowSql()) {
                val sqlLevel = setSqlLevel(Level.INFO)
                try {
                    runnable.run()
                } finally {
                    sqlLevel?.let { setSqlLevel(it) }
                }
            } else {
                runnable.run()
            }
        }

        @JvmStatic
        fun <T> disableLog(runnable: () -> T): T {
            return if (isShowSql()) {
                val sqlLevel = setSqlLevel(Level.INFO)
                try {
                    runnable()
                } finally {
                    sqlLevel?.let { setSqlLevel(it) }
                }
            } else {
                runnable()
            }
        }

        @JvmStatic
        fun enableLog(runnable: Runnable) {
            if (!isShowSql()) {
                val sqlLevel = setSqlLevel(Level.DEBUG)
                try {
                    runnable.run()
                } finally {
                    sqlLevel?.let { setSqlLevel(it) }
                }
            } else {
                runnable.run()
            }
        }

        @JvmStatic
        fun <T> enableLog(runnable: () -> T): T {
            return if (!isShowSql()) {
                val sqlLevel = setSqlLevel(Level.DEBUG)
                try {
                    runnable()
                } finally {
                    sqlLevel?.let { setSqlLevel(it) }
                }
            } else {
                runnable()
            }
        }

        fun Logger.total(total: Number) {
            try {
                MDC.put(MDC_SQL_TOTAL, total.toString())
                debug("total: {} rows", total)
            } finally {
                MDC.remove(MDC_SQL_TOTAL)
            }
        }

        fun Logger.result(result: String) {
            try {
                MDC.put(MDC_SQL_RESULT, result)
                debug("result: {}", result)
            } finally {
                MDC.remove(MDC_SQL_RESULT)
            }
        }

        fun Logger.retrieved(retrieved: Int) {
            try {
                MDC.put(MDC_SQL_RETRIEVED, retrieved.toString())
                debug("retrieved: {} rows", retrieved)
            } finally {
                MDC.remove(MDC_SQL_RETRIEVED)
            }
        }

        fun Logger.affected(affected: String) {
            try {
                MDC.put(MDC_SQL_AFFECTED, affected)
                debug("affected: {} rows", affected)
            } finally {
                MDC.remove(MDC_SQL_AFFECTED)
            }
        }

        fun Logger.start() {
            try {
                val current = System.currentTimeMillis()
                MDC.put(MDC_SQL_TIME_START, current.toString())
                debug("start: {} ms", current)
            } finally {
                MDC.remove(MDC_SQL_TIME_START)
            }
        }

        fun Logger.end() {
            try {
                val current = System.currentTimeMillis()
                MDC.put(MDC_SQL_TIME_END, current.toString())
                MDC.put(MDC_SQL_END, "END")
                debug("end: {} ms", current)
            } finally {
                MDC.remove(MDC_SQL_TIME_END)
                MDC.remove(MDC_SQL_END)
            }
        }

        fun Logger.offset(offset: Long) {
            MDC.put(MDC_SQL_OFFSET, offset.toString())
            debug("offset: {} rows", offset)
        }

        fun Logger.limit(limit: Int) {
            MDC.put(MDC_SQL_LIMIT, limit.toString())
            debug("limit: {} rows", limit)
        }
    }

    private val sqlCache: ConcurrentMap<String, SqlLogData> = ConcurrentHashMap()
    private val logger = LoggerFactory.getLogger(SqlAppender::class.java)
    private val sqlLogger = LoggerFactory.getLogger("SQL")

    override fun append(event: ILoggingEvent?) {
        val loggerName = event?.loggerName
        if (event == null || !isStarted || !sqlLogger.isInfoEnabled) {
            return
        }
        try {
            val traceid = event.mdcPropertyMap[HttpOperation.MDC_TRACEID]
            if (traceid != null) {
                MDC.put(HttpOperation.MDC_TRACEID, traceid)
            }
            val id = event.mdcPropertyMap[MDC_SQL_ID] ?: ""
            val isEnd = !event.mdcPropertyMap[MDC_SQL_END].isNullOrBlank()
            val key = "${traceid ?: event.threadName}:$id"
            val logData = sqlCache.computeIfAbsent(key) { SqlLogData(id) }
            val msg = event.formattedMessage
            when (loggerName) {
                "org.hibernate.SQL" -> {
                    if (!logData.sql.isNullOrBlank()) {
                        if (logData.end == null) {
                            logData.end = System.currentTimeMillis()
                        }
                        log(logData)
                        logData.params.removeIf { it.index <= logData.paramCount }
                    }
                    logData.sql = msg
                }

                LOG_SLOW -> {
                    //SlowQuery: 2896 milliseconds. SQL: 'HikariProxyPreparedStatement@803096561 wrapping com.mysql.cj.jdbc.ClientPreparedStatement:
                    logData.slowSql.add(msg)
                }

                //binding parameter [1] as [BIGINT] - [1704732344574808773]
                "org.hibernate.type.descriptor.sql.BasicBinder" -> {
                    val index = msg.substringAfter("[").substringBefore("]").toInt()
                    val type = msg.substringAfter("as [").substringBefore("]")
                    val value =
                        msg.substringAfter(" - [").substringBeforeLast("]").replace("\n", "\\n")
                    logData.params.add(
                        SqlLogParam(
                            index,
                            JavaTypeResolver.type(type)?.javaType,
                            value
                        )
                    )
                }

                else -> {
                    //start
                    val start = event.mdcPropertyMap[MDC_SQL_TIME_START]
                    if (!start.isNullOrBlank()) {
                        logData.start = start.toLong()
                    }
                    //total: {} rows
                    val total = event.mdcPropertyMap[MDC_SQL_TOTAL]
                    if (!total.isNullOrBlank()) {
                        logData.total = total.toLong()
                    }
                    //result: {}
                    val result = event.mdcPropertyMap[MDC_SQL_RESULT]
                    if (!result.isNullOrBlank()) {
                        logData.result = result
                    }
                    //{} rows retrieved
                    val retrieved = event.mdcPropertyMap[MDC_SQL_RETRIEVED]
                    if (!retrieved.isNullOrBlank()) {
                        logData.retrieved = retrieved.toInt()
                    }
                    //{} row affected
                    val affected = event.mdcPropertyMap[MDC_SQL_AFFECTED]
                    if (!affected.isNullOrBlank()) {
                        logData.affected = affected
                    }
                    //end: {} ms
                    val endTime = event.mdcPropertyMap[MDC_SQL_TIME_END]
                    if (!endTime.isNullOrBlank()) {
                        logData.end = endTime.toLong()
                    }
                    val offset = event.mdcPropertyMap[MDC_SQL_OFFSET]
                    if (!offset.isNullOrBlank()) {
                        logData.offset = offset.toLong()
                    }
                    val limit = event.mdcPropertyMap[MDC_SQL_LIMIT]
                    if (!limit.isNullOrBlank()) {
                        logData.limit = limit.toInt()
                    }
                    val error = event.mdcPropertyMap[MDC_SQL_ERROR]
                    if (!error.isNullOrBlank())
                        logData.error = error
                }
            }
            if (isEnd || (LOG_SLOW == loggerName && !isShowSql())) {
                if (logData.end == null) {
                    logData.end = System.currentTimeMillis()
                }
                log(logData)
                sqlCache.remove(key)
            }
        } catch (e: Exception) {
            logger.error("sql日志记录失败", e)
        }
    }

    private fun log(logData: SqlLogData) {
        val cost = logData.cost
        val slowSql = logData.slowSql
        if (logData.sql.isNullOrBlank()) {
            logData.sql = slowSql.joinToString("\n------\n")
        }

        if (!logData.error.isNullOrBlank()) {
            sqlLogger.error(AlarmMarker.noAlarmMarker, logData.toSql(timeoutAlarmMS))
        } else if (cost != null && timeoutAlarmMS > 0 && cost > timeoutAlarmMS) {
            val initialComment = "${logData.id}：执行速度慢(${cost / 1000}秒)"
            sqlLogger.warn(
                AlarmMarker(
                    message = initialComment,
                    timeout = true,
                    level = Level.WARN
                ),
                logData.toSql(timeoutAlarmMS)
            )
        } else if (slowSql.isNotEmpty()) {
            sqlLogger.warn(logData.toSql(timeoutAlarmMS))
        } else
            sqlLogger.info(logData.toSql(timeoutAlarmMS))
    }

}