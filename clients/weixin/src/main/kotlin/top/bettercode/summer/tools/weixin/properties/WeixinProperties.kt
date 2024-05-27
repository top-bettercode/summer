package top.bettercode.summer.tools.weixin.properties

import top.bettercode.summer.tools.lang.client.ClientProperties

/**
 * @author Peter Wu
 */
open class WeixinProperties(
    /**
     * 平台名称
     */
    platformName: String
) : ClientProperties(platformName) {
    companion object {
        const val OPEN_ID_NAME = "openId"
    }

    open var basicAccessTokenUrl: String =
        "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={0}&secret={1}"

    var appId: String = ""
    var secret: String = ""
    var token: String = ""
    var aesKey: String = ""


    /**
     * access_token 及 jsapi_ticket 有效期7200秒
     */
    var cacheSeconds = 7200L

    /**
     * 微信接口请求最大重试次数
     */
    var maxRetries = 2

}
