package top.bettercode.summer.tools.lang.log

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.slf4j.*
import org.springframework.util.Assert
import top.bettercode.summer.tools.lang.operation.HttpOperation
import top.bettercode.summer.tools.lang.util.JavaTypeResolver
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class SqlAppender(private val timeoutAlarmMS: Long) : AppenderBase<ILoggingEvent>() {
    companion object {
        const val MDC_SQL_ERROR = "SQL_ERROR"
        const val MDC_SQL_ID = "SQL_ID"
        const val MDC_SQL_DISABLE_LOG = "SQL_DISABLE_LOG"
        const val MDC_SQL_END = "SQL_END"
        const val MDC_SQL_TOTAL = "SQL_TOTAL"
        const val MDC_SQL_RESULT = "SQL_RESULT"
        const val MDC_SQL_RETRIEVED = "SQL_RETRIEVED"
        const val MDC_SQL_AFFECTED = "SQL_AFFECTED"
        const val MDC_SQL_COST = "SQL_COST"
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
                    MDC.put(MDC_SQL_DISABLE_LOG, "true")
                    runnable.run()
                } finally {
                    sqlLevel?.let { setSqlLevel(it) }
                    MDC.remove(MDC_SQL_DISABLE_LOG)
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
                    MDC.put(MDC_SQL_DISABLE_LOG, "true")
                    runnable()
                } finally {
                    sqlLevel?.let { setSqlLevel(it) }
                    MDC.remove(MDC_SQL_DISABLE_LOG)
                }
            } else {
                runnable()
            }
        }

        @JvmStatic
        fun isLogEnabled() = MDC.get(MDC_SQL_DISABLE_LOG) == null

        fun Logger.total(total: Number) {
            try {
                MDC.put(MDC_SQL_TOTAL, total.toString())
                info("total: {} rows", total)
            } finally {
                MDC.remove(MDC_SQL_TOTAL)
            }
        }

        fun Logger.result(result: String) {
            try {
                MDC.put(MDC_SQL_RESULT, result)
                info("result: {}", result)
            } finally {
                MDC.remove(MDC_SQL_RESULT)
            }
        }

        fun Logger.retrieved(retrieved: Int) {
            try {
                MDC.put(MDC_SQL_RETRIEVED, retrieved.toString())
                info("retrieved: {} rows", retrieved)
            } finally {
                MDC.remove(MDC_SQL_RETRIEVED)
            }
        }

        fun Logger.affected(affected: Any) {
            try {
                MDC.put(MDC_SQL_AFFECTED, affected.toString())
                debug("affected: {} rows", affected)
            } finally {
                MDC.remove(MDC_SQL_AFFECTED)
            }
        }

        fun Logger.cost(cost: Long) {
            try {
                MDC.put(MDC_SQL_COST, cost.toString())
                MDC.put(MDC_SQL_END, "END")
                if (cost > 2 * 1000) {
                    warn("cost: {} ms", cost)
                } else
                    info("cost: {} ms", cost)
            } finally {
                MDC.remove(MDC_SQL_COST)
                MDC.remove(MDC_SQL_END)
                MDC.remove(MDC_SQL_OFFSET)
                MDC.remove(MDC_SQL_LIMIT)
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

    public override fun append(event: ILoggingEvent?) {
        if (event == null || !isStarted || !logger.isInfoEnabled || !isLogEnabled()) {
            return
        }
        try {
            val traceid = event.mdcPropertyMap[HttpOperation.MDC_TRACEID]
                ?: event.threadName
            val id = event.mdcPropertyMap[MDC_SQL_ID] ?: ""
            val end = !event.mdcPropertyMap[MDC_SQL_END].isNullOrBlank()
            val key = "$traceid:$id"
            var sqlLogData = sqlCache.computeIfAbsent(key) { SqlLogData(id) }
            val msg = event.formattedMessage
            when (event.loggerName) {
                "org.hibernate.SQL" -> {
                    if (!sqlLogData.sql.isNullOrBlank()) {
                        sqlCache.remove(key)
                        log(sqlLogData)
                        sqlLogData = sqlCache.computeIfAbsent(key) { SqlLogData(id) }
                    }
                    sqlLogData.sql = msg
                }

                LOG_SLOW -> {
                    //SlowQuery: 2896 milliseconds. SQL: 'HikariProxyPreparedStatement@803096561 wrapping com.mysql.cj.jdbc.ClientPreparedStatement:
                    sqlLogData.slowSql.add(msg)
                }

                "org.hibernate.type.descriptor.sql.BasicBinder" -> {
                    val regex = Regex("""\[(.*?)]""")
                    val matches = regex.findAll(msg).map { it.groupValues[1] }.toList()
                    val index = matches[0].toInt()
                    sqlLogData.params.add(
                        SqlLogParam(
                            index,
                            JavaTypeResolver.type(matches[1])?.javaType,
                            matches[2]
                        )
                    )
                }

                else -> {
                    //total: {} rows
                    val total = event.mdcPropertyMap[MDC_SQL_TOTAL]
                    if (!total.isNullOrBlank()) {
                        sqlLogData.total = total.toLong()
                    }
                    //result: {}
                    val result = event.mdcPropertyMap[MDC_SQL_RESULT]
                    if (!result.isNullOrBlank()) {
                        sqlLogData.result = result
                    }
                    //{} rows retrieved
                    val retrieved = event.mdcPropertyMap[MDC_SQL_RETRIEVED]
                    if (!retrieved.isNullOrBlank()) {
                        sqlLogData.retrieved = retrieved.toInt()
                    }
                    //{} row affected
                    val affected = event.mdcPropertyMap[MDC_SQL_AFFECTED]
                    if (!affected.isNullOrBlank()) {
                        sqlLogData.affected = affected.toInt()
                    }
                    //cost: {} ms
                    val cost = event.mdcPropertyMap[MDC_SQL_COST]
                    if (!cost.isNullOrBlank()) {
                        sqlLogData.cost = cost.toLong()
                    }
                    val offset = event.mdcPropertyMap[MDC_SQL_OFFSET]
                    if (!offset.isNullOrBlank()) {
                        sqlLogData.offset = offset.toLong()
                    }
                    val limit = event.mdcPropertyMap[MDC_SQL_LIMIT]
                    if (!limit.isNullOrBlank()) {
                        sqlLogData.limit = limit.toInt()
                    }
                    val error = event.mdcPropertyMap[MDC_SQL_ERROR]
                    if (!error.isNullOrBlank())
                        sqlLogData.error = error
                }
            }
            if (end || (LOG_SLOW == event.loggerName && !isShowSql())) {
                log(sqlLogData)
                sqlCache.remove(key)
            }
        } catch (e: Exception) {
            logger.error("sql日志记录失败", e)
        }
    }

    private fun log(
        sqlLogData: SqlLogData
    ) {
        val cost = sqlLogData.cost
        val slowSql = sqlLogData.slowSql
        if (sqlLogData.sql.isNullOrBlank()) {
            sqlLogData.sql = slowSql.joinToString("\n------\n")
        }

        if (!sqlLogData.error.isNullOrBlank()) {
            logger.error(
                MarkerFactory.getMarker(AlarmAppender.NO_ALARM_LOG_MARKER),
                sqlLogData.toString()
            )
        } else if (cost != null && timeoutAlarmMS > 0 && cost > timeoutAlarmMS) {
            val initialComment = "${sqlLogData.id}：执行速度慢(${cost / 1000}秒)"
            logger.warn(
                AlarmMarker(initialComment, true),
                sqlLogData.toString()
            )
        } else if (slowSql.isNotEmpty()) {
            logger.warn(sqlLogData.toString())
        } else
            logger.info(sqlLogData.toString())
    }

}