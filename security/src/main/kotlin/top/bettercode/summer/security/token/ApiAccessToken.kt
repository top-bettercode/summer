package top.bettercode.summer.security.token

import com.fasterxml.jackson.annotation.*
import org.springframework.security.core.userdetails.UserDetails
import top.bettercode.summer.security.userdetails.AdditionalUserDetails
import java.io.Serializable
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
class ApiAccessToken : Serializable {
    @JsonProperty("token_type")
    var tokenType: String? = null

    @JsonProperty("access_token")
    var accessToken: String? = null
    private var expiresAt: Instant? = null

    @JsonProperty("refresh_token")
    var refreshToken: String? = null

    @JsonProperty("scope")
    var scope: String? = null
    private var additionalInformation: MutableMap<String, Any?> = mutableMapOf()

    @get:JsonIgnore
    var apiAuthenticationToken: ApiToken? = null
        private set

    constructor()
    constructor(apiToken: ApiToken) {
        this.apiAuthenticationToken = apiToken
        val accessToken = apiToken.accessToken
        val userDetails = apiToken.userDetails
        tokenType = "bearer"
        this.accessToken = accessToken.tokenValue
        expiresAt = accessToken.expiresAt
        refreshToken = apiToken.refreshToken.tokenValue
        scope = apiToken.scope
        additionalInformation = if (userDetails is AdditionalUserDetails) userDetails.additionalInformation else mutableMapOf()
    }

    @get:JsonIgnore
    val userDetails: UserDetails?
        get() = apiAuthenticationToken?.userDetails

    @get:JsonProperty("expires_in")
    var expiresIn: Int
        //--------------------------------------------
        get() = if (expiresAt != null) java.lang.Long.valueOf(
                (expiresAt!!.toEpochMilli() - System.currentTimeMillis()) / 1000L)
                .toInt() else 0
        protected set(delta) {
            expiresAt = Instant.ofEpochSecond(System.currentTimeMillis() / 1000L + delta)
        }

    @JsonAnyGetter
    fun getAdditionalInformation(): Map<String, Any?> {
        return additionalInformation
    }

    fun setExpiresAt(expiresAt: Instant?) {
        this.expiresAt = expiresAt
    }

    @JsonAnySetter
    fun setAdditionalInformation(name: String, value: Any?) {
        additionalInformation[name] = value
    }

    fun setAdditionalInformation(additionalInformation: MutableMap<String, Any?>) {
        this.additionalInformation = additionalInformation
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
