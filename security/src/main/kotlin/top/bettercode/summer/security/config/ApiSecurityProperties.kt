package top.bettercode.summer.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.util.AntPathMatcher
import top.bettercode.summer.tools.lang.util.ArrayUtil.contains
import top.bettercode.summer.tools.lang.util.ArrayUtil.isEmpty

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.security")
open class ApiSecurityProperties {
    var refreshTokenValiditySeconds = 60 * 60 * 24 * 30 // default 30 days.
    var accessTokenValiditySeconds = 60 * 60 * 12 // default 12 hours.

    /**
     * 默认不过期
     */
    var userDetailsValiditySeconds = -1

    /**
     * security.url-filter.ignored.
     */
    var urlFilterIgnored: Array<String> = arrayOf<String>()
    var sessionCreationPolicy = SessionCreationPolicy.STATELESS

    /**
     * 是否禁用同源策略.
     */
    var frameOptionsDisable = true
    var supportClientCache = true
    //--------------------------------------------
    /**
     * 登录时是否踢出前一个登录用户,全局配置
     */
    var loginKickedOut = false

    /**
     * 登录时是否踢出前一个登录用户，针对特殊scope
     */
    var loginKickedOutScopes = arrayOf<String>()

    /**
     * 是否兼容旧toekn名称
     */
    var compatibleAccessToken = false
    var clientId: String? = null
    var clientSecret: String? = null

    /**
     * 支持的权限范围
     */
    var supportScopes: Array<String> = arrayOf("app")

    //--------------------------------------------
    fun ignored(path: String): Boolean {
        if (urlFilterIgnored.isEmpty()) {
            return false
        }
        val antPathMatcher = AntPathMatcher()
        for (pattern in urlFilterIgnored) {
            if (antPathMatcher.match(pattern, path)) {
                return true
            }
        }
        return false
    }

    fun needKickedOut(scope: String): Boolean {
        return loginKickedOut || loginKickedOutScopes.contains(scope)
    }
}
