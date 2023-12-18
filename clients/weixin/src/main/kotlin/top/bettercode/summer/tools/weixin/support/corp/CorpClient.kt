package top.bettercode.summer.tools.weixin.support.corp

import org.slf4j.MarkerFactory
import org.springframework.web.client.getForObject
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.weixin.properties.CorpProperties
import top.bettercode.summer.tools.weixin.support.IWeixinCache
import top.bettercode.summer.tools.weixin.support.WeixinClient
import top.bettercode.summer.tools.weixin.support.corp.entity.CorpWebPageAccessToken
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * 公众号接口
 *
 * @author Peter Wu
 */
@LogMarker(CorpClient.MARKER)
open class CorpClient(
    properties: CorpProperties,
    cache: IWeixinCache
) :
    WeixinClient<CorpProperties>(
        properties,
        cache,
        MARKER
    ) {

    companion object {
        const val MARKER = "wxcorp"
    }

    init {
        val url =
            "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=#wechat_redirect"
        val authenticationUrl = String.format(
                url,
                properties.appId,
                URLEncoder.encode(properties.oauthUrl, StandardCharsets.UTF_8),
                "snsapi_base"
        )
        log.info(MarkerFactory.getMarker(marker), "authenticationUrl:{}", authenticationUrl)
    }

    @JvmOverloads
    open fun getWebPageAccessToken(code: String, retries: Int = 1): CorpWebPageAccessToken {
        val result = getForObject<CorpWebPageAccessToken>(
            "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?access_token=%s&code=CODE",
            getStableAccessToken()
        )
        return if (result.isOk) {
            result
        } else if (40001 == result.errcode || 42001 == result.errcode) {
            //40001 access_token无效
            //42001 access_token过期
            clearStableTokenCache()
            getWebPageAccessToken(code, retries)
        } else if (retries < properties.maxRetries) {
            getWebPageAccessToken(code, retries + 1)
        } else {
            throw clientSysException("网页授权失败：${result.errmsg}", result)
        }
    }

}