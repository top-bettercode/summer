package top.bettercode.summer.security.token

import org.springframework.security.core.userdetails.UserDetails
import java.io.Serializable

/**
 * @author Peter Wu
 */
class ApiToken : Serializable {
    //--------------------------------------------
    lateinit var scope: String
    lateinit var accessToken: Token
    lateinit var refreshToken: Token
    lateinit var userDetailsInstantAt: InstantAt
    lateinit var userDetails: UserDetails

    constructor()
    constructor(
            scope: String, accessToken: Token,
            refreshToken: Token, userDetailsInstantAt: InstantAt, userDetails: UserDetails
    ) {
        this.scope = scope
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.userDetailsInstantAt = userDetailsInstantAt
        this.userDetails = userDetails
    }

    val username: String
        get() = userDetails.username

    fun toApiToken(): ApiAccessToken {
        return ApiAccessToken(this)
    }

    companion object
}
