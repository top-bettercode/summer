package top.bettercode.summer.security.token

import org.springframework.security.core.userdetails.UserDetails

/**
 * @author Peter Wu
 */
interface IRevokeTokenService {
    fun revokeToken(principal: UserDetails)
}
