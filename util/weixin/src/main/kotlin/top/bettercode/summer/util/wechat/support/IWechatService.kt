package top.bettercode.summer.util.wechat.support

import top.bettercode.summer.util.wechat.support.miniprogram.entity.PhoneInfo

/**
 * @author Peter Wu
 */
interface IWechatService {

    fun forceLogin(): Boolean {
        return false
    }

    fun oauth(openId: String): String? {
        throw UnsupportedOperationException()
    }

    fun phoneOauth(phoneInfo: PhoneInfo): String? {
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

    }

}