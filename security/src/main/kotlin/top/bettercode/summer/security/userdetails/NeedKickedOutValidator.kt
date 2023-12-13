package top.bettercode.summer.security.userdetails

import org.springframework.security.core.userdetails.UserDetails

/**
 * @author Peter Wu
 */
interface NeedKickedOutValidator {

    fun validate(clientId: String, scope: Set<String>, userDetails: UserDetails): Boolean
}
