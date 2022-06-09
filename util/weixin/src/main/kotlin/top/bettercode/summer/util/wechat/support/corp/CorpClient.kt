package top.bettercode.summer.util.wechat.support.corp

import org.slf4j.MarkerFactory
import org.springframework.web.client.getForObject
import top.bettercode.summer.util.wechat.config.ICorpProperties
import top.bettercode.summer.util.wechat.support.WeixinClient
import top.bettercode.summer.util.wechat.support.corp.entity.CorpWebPageAccessToken
import java.net.URLEncoder

/**
 * 公众号接口
 *
 * @author Peter Wu
 */
class CorpClient(properties: ICorpProperties) :
    WeixinClient<ICorpProperties>(
        properties,
        "第三方接口",
        "微信企业号",
        "wexin-corp"
    ), ICorpClient {


    init {
        val url =
            "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=#wechat_redirect"
        val authenticationUrl = String.format(
            url,
            getAppId(),
            URLEncoder.encode(properties.oauthUrl, "UTF-8"),
            "snsapi_base"
        )
        log.info(MarkerFactory.getMarker(logMarker), "authenticationUrl:{}", authenticationUrl)
    }



    override fun getWebPageAccessToken(code: String): CorpWebPageAccessToken {
        return getForObject(
            "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?access_token=%s&code=CODE",
            getBaseAccessToken()
        )
    }

}