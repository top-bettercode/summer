package top.bettercode.summer.tools.pay.support

/**
 *
 * @author Peter Wu
 */
class WeixinPayException(message: String, val response: Any?) : RuntimeException(message) {
    //serrialVersionUID
    private val serialVersionUID = 1L
}