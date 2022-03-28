package top.bettercode.summer.util.wechat.support

/**
 * @author Peter Wu
 */
interface IWechatService {

    fun oauth(openId: String): String?

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