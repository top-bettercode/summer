package top.bettercode.summer.tools.weixin.support.corp

import org.slf4j.MarkerFactory
import org.springframework.web.client.getForObject
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.weixin.config.ICorpProperties
import top.bettercode.summer.tools.weixin.support.WeixinClient
import top.bettercode.summer.tools.weixin.support.corp.CorpClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.weixin.support.corp.entity.CorpWebPageAccessToken
import java.net.URLEncoder

/**
 * 公众号接口
 *
 * @author Peter Wu
 */
@LogMarker(LOG_MARKER)
class CorpClient(properties: ICorpProperties) :
    WeixinClient<ICorpProperties>(
        properties,
        "第三方接口",
        "微信企业号",
        LOG_MARKER
    ), ICorpClient {

    companion object {
        const val LOG_MARKER = "wxcorp"
    }

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