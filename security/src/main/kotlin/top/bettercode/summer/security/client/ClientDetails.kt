package top.bettercode.summer.security.client

/**
 *
 * @author Peter Wu
 */
open class ClientDetails {

    var clientId: String = ""

    var clientSecret: String = ""

    var scope = setOf("app")

    var accessTokenValiditySeconds: Int = 60 * 60 * 12 // default 12 hours.

    var refreshTokenValiditySeconds: Int = 60 * 60 * 24 * 30 // default 30 days

    //--------------------------------------------

    /**
     * 登录时是否踢出前一个登录用户,全局配置
     */
    var isLoginKickedOut = false

    /**
     * 登录时是否踢出前一个登录用户，针对特殊scope
     */
    var loginKickedOutScopes = arrayOf<String>()

    fun needKickedOut(scope: Set<String>): Boolean {
        return isLoginKickedOut || loginKickedOutScopes.any { scope.contains(it) }
    }

    open fun supportScope(scope: Collection<String>): Boolean {
        return if (this.scope.isEmpty()) {
            true
        } else
            this.scope.containsAll(scope)
    }
}