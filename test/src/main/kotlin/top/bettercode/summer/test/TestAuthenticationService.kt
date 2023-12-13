package top.bettercode.summer.test

/**
 *
 * @author Peter Wu
 */
interface TestAuthenticationService {

    fun loadAuthentication(clientd: String, scope: Set<String>, username: String)

}