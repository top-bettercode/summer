package top.bettercode.summer.tools.lang.client

/**
 *
 * @author Peter Wu
 */
interface ClientResponse {

    /**
     * @return 是否成功
     */
    val isOk: Boolean

    /**
     * @return 响应消息
     */
    val message: String?

}