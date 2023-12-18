package top.bettercode.summer.security.userdetails

import jakarta.servlet.http.HttpServletRequest
import top.bettercode.summer.security.token.StoreToken

/**
 * @author Peter Wu
 */
interface LoginListener {

    fun beforeLogin(request: HttpServletRequest, grantType: String, clientId: String, scope: Set<String>) {
    }

    fun afterLogin(storeToken: StoreToken, request: HttpServletRequest) {}
}
