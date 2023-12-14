package top.bettercode.summer.security.token

import org.springframework.security.core.userdetails.UserDetails
import java.io.Serializable

/**
 * @author Peter Wu
 */
class StoreToken : Serializable {
    //--------------------------------------------
    lateinit var clientId: String
    lateinit var scope: Set<String>
    lateinit var accessToken: Token
    lateinit var refreshToken: Token
    lateinit var userDetails: UserDetails

    constructor()
    constructor(clientId: String, scope: Set<String>, accessToken: Token,
                refreshToken: Token, userDetails: UserDetails
    ) {
        this.clientId = clientId
        this.scope = scope
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.userDetails = userDetails
    }

    val username: String
        get() = userDetails.username

    fun toId(): TokenId {
        return TokenId(this.clientId, this.username)
    }

    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID = 1L
    }
}
