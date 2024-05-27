package top.bettercode.summer.tools.lang.client

/**
 * @author Peter Wu
 */
open class ClientProperties(
    /**
     * 平台名称
     */
    var platformName: String
) {
    /**
     * 请求超时超过多少秒报警，-1表示不报警，默认-1.
     */
    var timeoutAlarmSeconds = -1

    /**
     * 请求连接超时时间秒数
     */
    var connectTimeout = 10

    /**
     * 请求读取超时时间秒数
     */
    var readTimeout = 10

}
