package top.bettercode.summer.tools.weixin.properties

/**
 * @author Peter Wu
 */
interface IWeixinProperties {
    val basicAccessTokenUrl: String
        get() = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={0}&secret={1}"
    val appId: String
    val secret: String
    val token: String
    val aesKey: String
    /**
     * 请求超时超过多少秒报警，-1表示不报警，默认-1.
     */
    var timeoutAlarmSeconds: Int
    val connectTimeout: Int
    val readTimeout: Int
    val cacheSeconds: Long
    val maxRetries: Int

    companion object {
        const val OPEN_ID_NAME = "openId"
    }
}
