package top.bettercode.summer.test

/**
 *
 * @author Peter Wu
 */
interface TestUserDetailsService {

    fun loadAuthenticationToken(scope: String, username: String)

}