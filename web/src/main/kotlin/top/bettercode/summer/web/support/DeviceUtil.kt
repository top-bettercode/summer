package top.bettercode.summer.web.support

import javax.servlet.http.HttpServletRequest

/**
 * 客户端设备工具
 *
 * @author Peter Wu
 */
object DeviceUtil {
    /**
     * @param request request
     * @return UserAgent
     */
    fun getUserAgent(request: HttpServletRequest): String? {
        val headers = request.getHeaders("user-agent")
        return if (headers.hasMoreElements()) {
            headers.nextElement()
        } else {
            null
        }
    }
}
