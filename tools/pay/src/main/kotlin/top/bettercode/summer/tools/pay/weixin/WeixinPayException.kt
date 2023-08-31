package top.bettercode.summer.tools.pay.weixin

/**
 *
 * @author Peter Wu
 */
class WeixinPayException(message: String, val response: Any? = null) : RuntimeException(message) {
    //serrialVersionUID
    private val serialVersionUID = 1L
}