package top.bettercode.summer.security.token

import java.io.Serializable

/**
 *
 * @author Peter Wu
 */
class TokenId(
        val clientId: String,
        val username: String
) : Serializable {

    override fun toString(): String {
        return "$clientId:$username"
    }

    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID = 1L
    }
}