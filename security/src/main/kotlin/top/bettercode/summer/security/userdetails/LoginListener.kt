package top.bettercode.summer.security.userdetails

import top.bettercode.summer.security.token.StoreToken
import javax.servlet.http.HttpServletRequest

/**
 * @author Peter Wu
 */
interface LoginListener {

    fun beforeLogin(request: HttpServletRequest, grantType: String, clientId: String, scope: Set<String>) {
        beforeLogin(request, grantType, clientId, scope.first())
    }

    fun beforeLogin(request: HttpServletRequest, grantType: String, clientId: String, scope: String) {
    }

    fun afterLogin(storeToken: StoreToken, request: HttpServletRequest) {}
}
