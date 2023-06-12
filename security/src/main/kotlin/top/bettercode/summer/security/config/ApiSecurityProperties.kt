package top.bettercode.summer.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.util.AntPathMatcher

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
    var isFrameOptionsDisable = true
    var isSupportClientCache = true
    //--------------------------------------------
    /**
     * 登录时是否踢出前一个登录用户,全局配置
     */
    var isLoginKickedOut = false

    /**
     * 登录时是否踢出前一个登录用户，针对特殊scope
     */
    var loginKickedOutScopes = arrayOf<String>()

    /**
     * 是否兼容旧toekn名称
     */
    var isCompatibleAccessToken = false
    var clientId: String? = null
    var clientSecret: String? = null

    /**
     * 支持的权限范围
     */
    var supportScopes: Array<String> = arrayOf("app")

    var secureRandomSeed: String? = null

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
        return isLoginKickedOut || loginKickedOutScopes.contains(scope)
    }
}
