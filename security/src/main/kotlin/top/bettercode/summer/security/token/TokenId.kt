package top.bettercode.summer.security.token

/**
 *
 * @author Peter Wu
 */
class TokenId(
        val clientId: String,
        val username: String
) {
    override fun toString(): String {
        return "$clientId:$username"
    }
}