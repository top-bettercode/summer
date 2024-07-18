package top.bettercode.summer.tools.lang.log


/**
 * alarm 配置
 *
 * @author Peter Wu
 */
open class AlarmProperties {

    /**
     * 告警的logger
     */
    var logger = arrayOf("root")

    /**
     * 不告警的logger
     */
    var ignoredWarnLogger = arrayOf<String>()

    /**
     * 告警缓存时间
     */
    var cacheSeconds = 5 * 60L

    /**
     * 告警超时缓存时间
     */
    var timeoutCacheSeconds = 2 * 60 * 60L

    /**
     * 缓存日志条数
     */
    var cyclicBufferSize = 20

    /**
     * 启动日志
     */
    var startedMsg: String = "^Started .*? in .*? seconds \\(.*?\\)$"

    //--------------------------------------------

    /**
     * 日志存储路径
     */
    var logsPath: String = ""

    /**
     * 日志访问地址
     */
    var actuatorAddress: String = ""

    /**
     * 接口访问地址
     */
    var apiAddress: String = ""
    /**
     * 日志访问路径
     */
    var managementLogPath: String = ""

    /**
     * 日志访问主机名
     */
    var managementHostName: String = ""
    /**
     * 日志访问端口
     */
    var managementPort: Int = 0

    /**
     * 日志格式
     */
    var logPattern: String = DEFAULT_LOG_PATTERN

    /**
     * 告警标题
     */
    var warnTitle: String = ""


    companion object {
        const val DEFAULT_LOG_PATTERN =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} \${LOG_LEVEL_PATTERN:%5p} [%-6.6t] %-40.40logger{39} %20file:%-3line %X{traceid}: %m%n\${LOG_EXCEPTION_CONVERSION_WORD:%wEx}"
    }
}