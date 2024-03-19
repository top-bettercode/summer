package top.bettercode.summer.tools.weixin.properties

/**
 * @author Peter Wu
 */
open class WeixinProperties : IWeixinProperties {
    override var appId: String = ""
    override var secret: String = ""
    override var token: String = ""
    override var aesKey: String = ""

    /**
     * 请求超时超过多少秒报警，-1表示不报警，默认-1.
     */
    override var timeoutAlarmSeconds: Int = -1

    /**
     * 请求连接超时时间秒数
     */
    override var connectTimeout = 10

    /**
     * 请求读取超时时间秒数
     */
    override var readTimeout = 10

    /**
     * access_token 及 jsapi_ticket 有效期7200秒
     */
    override var cacheSeconds = 7200L

    /**
     * 微信接口请求最大重试次数
     */
    override var maxRetries = 2

    /**
     * 授权请求路径
     */
    var oauthMappingPath: String = ""

}
