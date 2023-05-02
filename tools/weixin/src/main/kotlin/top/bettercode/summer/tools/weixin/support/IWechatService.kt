package top.bettercode.summer.tools.weixin.support

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.weixin.support.corp.entity.CorpWebPageAccessToken
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.JsSession
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.PhoneInfo
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.SnsapiUserinfo
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.WebPageAccessToken

/**
 * @author Peter Wu
 */
interface IWechatService {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun forceLogin(): Boolean {
        return false
    }

    fun miniOauth(jsSession: JsSession): WechatToken {
        throw UnsupportedOperationException()
    }

    fun oauth(
            webPageAccessToken: WebPageAccessToken,
            snsapiUserinfo: SnsapiUserinfo?
    ): WechatToken {
        throw UnsupportedOperationException()
    }

    fun corpOauth(corpWebPageAccessToken: CorpWebPageAccessToken): WechatToken {
        throw UnsupportedOperationException()
    }

    fun phoneOauth(phoneInfo: PhoneInfo): WechatToken {
        throw UnsupportedOperationException()
    }

    fun receive(
            timestamp: String,
            nonce: String,
            openid: String,
            encrypt_type: String,
            msg_signature: String,
            content: String
    ) {
        logger.warn("$timestamp $nonce $openid $encrypt_type $msg_signature receive message: $content")
    }

}