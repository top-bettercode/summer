package top.bettercode.summer.security.token

import com.fasterxml.jackson.annotation.*
import org.springframework.security.core.userdetails.UserDetails
import top.bettercode.summer.security.userdetails.AdditionalUserDetails
import top.bettercode.summer.web.serializer.annotation.JsonSetToString
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
open class AccessToken : IAccessToken {

    @JsonProperty("token_type")
    var tokenType: String? = null

    @JsonProperty("access_token")
    var accessToken: String? = null
    private var expiresAt: Instant? = null

    @JsonProperty("refresh_token")
    var refreshToken: String? = null

    @JsonSetToString(extended = false)
    @JsonProperty("scope")
    var scope: Set<String>? = null

    private var additionalInformation: MutableMap<String, Any?> = mutableMapOf()

    @get:JsonIgnore
    var apiAuthenticationToken: StoreToken? = null
        private set

    constructor()
    constructor(storeToken: StoreToken) {
        this.apiAuthenticationToken = storeToken
        val accessToken = storeToken.accessToken
        val userDetails = storeToken.userDetails
        tokenType = "bearer"
        this.accessToken = accessToken.tokenValue
        expiresAt = accessToken.expiresAt
        refreshToken = storeToken.refreshToken.tokenValue
        scope = storeToken.scope
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
        @Suppress("ConstPropertyName")
        private const val serialVersionUID = 1L
    }
}
