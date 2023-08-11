package top.bettercode.summer.security.userdetails

import top.bettercode.summer.security.token.ApiToken
import javax.servlet.http.HttpServletRequest

/**
 * @author Peter Wu
 */
interface LoginListener {
    @JvmDefault
    fun beforeLogin(request: HttpServletRequest?, grantType: String?, scope: String?) {}
    @JvmDefault
    fun afterLogin(apiToken: ApiToken?, request: HttpServletRequest?) {}
}
