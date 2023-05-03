package top.bettercode.summer.security.userdetails

import org.springframework.security.core.userdetails.UserDetails

/**
 * @author Peter Wu
 */
interface UserDetailsValidator {
    fun validate(userDetails: UserDetails)
}
