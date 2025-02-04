package top.bettercode.summer.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.DigestUtils
import java.nio.charset.StandardCharsets

/**
 * 日志访问权限 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.management.auth")
open class ManagementAuthProperties {

    var enabled = true

    //--------------------------------------------
    var pattern = arrayOf<String>()
    var ignorePattern = arrayOf("/health", "/logs/**", DEFAULT_LOGIN_PAGE)

    /**
     * 访问授权有效时间，单位：秒
     */
    var maxAge = 12 * 60 * 60
    var username = "madmin"
    var password: String? = null

    val authKey: String
        //--------------------------------------------
        get() = DigestUtils.md5DigestAsHex(
            "$username:$password".toByteArray(
                StandardCharsets.UTF_8
            )
        )

    companion object {
        const val DEFAULT_LOGIN_PAGE = "/login"

    }
}