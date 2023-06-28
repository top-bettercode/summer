package top.bettercode.summer.tools.weixin.support

/**
 *
 * @author Peter Wu
 */
class WeixinException(message:String, val response: Any) :RuntimeException(message){
    //serrialVersionUID
    private  val serialVersionUID = 1L
}