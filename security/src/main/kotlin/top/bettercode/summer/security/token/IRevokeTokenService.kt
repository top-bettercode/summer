package top.bettercode.summer.security.token

import org.springframework.security.core.userdetails.UserDetails

/**
 * @author Peter Wu
 */
fun interface IRevokeTokenService {
    fun revokeToken(principal: UserDetails)
}
