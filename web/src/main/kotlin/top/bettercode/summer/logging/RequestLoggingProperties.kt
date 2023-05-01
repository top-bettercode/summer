package top.bettercode.summer.logging

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * RequestLogging 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.logging.request")
class RequestLoggingProperties {
    //--------------------------------------------
    /**
     * 是否启用
     */
    var isEnabled = true

    /**
     * 是否包含请求体
     */
    var isIncludeRequestBody = true

    /**
     * 是否包含响应体
     */
    var isIncludeResponseBody = true

    /**
     * 是否包含错误追踪栈
     */
    var isIncludeTrace = true

    /**
     * 是否格式化日志
     */
    var isFormat = true

    /**
     * 强制记录日志
     */
    var isForceRecord = false

    /**
     * 请求超时警报时间秒数
     */
    var timeoutAlarmSeconds = 2

    /**
     * 忽略超时接口
     */
    var ignoredTimeoutPath = arrayOf<String>()

    /**
     * 需要记录日志的 Controller类名前缀.如果为空记录所有 Controller类.
     */
    var handlerTypePrefix = arrayOf<String>()

    /**
     * Comma-separated list of paths to exclude from the default logging paths.
     */
    var ignored = arrayOf("/**/*.js", "/**/*.gif", "/**/*.jpg", "/**/*.png", "/**/*.css", "/**/*.ico")

    /**
     * 额外包含的需要记录日志的路径
     */
    var includePath = arrayOf<String>()

    /**
     * 加密参数名
     */
    var encryptParameters = arrayOf<String>()

    /**
     * 加密请求头名
     */
    var encryptHeaders = arrayOf<String>()

    /**
     * 过滤不记录为ERROR日志的状态码
     */
    var ignoredErrorStatusCode = arrayOf(401, 403, 404, 405, 406)
}