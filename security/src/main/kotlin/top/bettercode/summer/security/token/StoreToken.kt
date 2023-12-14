package top.bettercode.summer.security.token

import org.springframework.security.core.userdetails.UserDetails
import java.io.Serializable

/**
 * @author Peter Wu
 */
class StoreToken(
        var id: TokenId,
        var clientId: String,
        var scope: Set<String>,
        var accessToken: Token,
        var refreshToken: Token,
        var userDetails: UserDetails) : Serializable {


    val username: String
        get() = this.userDetails.username

    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID = 1L
    }
}
