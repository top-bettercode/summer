package top.bettercode.summer.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.util.AntPathMatcher
import top.bettercode.summer.security.client.ClientDetails

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.security")
open class ApiSecurityProperties : ClientDetails() {

    /**
     * security.url-filter-ignored.
     */
    var urlFilterIgnored: Array<String> = arrayOf()

    var sessionCreationPolicy = SessionCreationPolicy.STATELESS

    /**
     * 是否禁用同源策略.
     */
    var isFrameOptionsDisable = true
    var isSupportClientCache = true
    //--------------------------------------------

    /**
     * 是否兼容旧toekn名称
     */
    var isCompatibleAccessToken = false

    var secureRandomSeed: String? = null

    /**
     * 以scope 为 clientId
     */
    var  isScopeClientId = false

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

}
