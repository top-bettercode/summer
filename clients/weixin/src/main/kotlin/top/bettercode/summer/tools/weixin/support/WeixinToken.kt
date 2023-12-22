package top.bettercode.summer.tools.weixin.support

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.web.serializer.annotation.JsonSetToString

/**
 *
 * @author Peter Wu
 */
open class WeixinToken @JvmOverloads constructor(message: String? = null) {

    @JsonProperty("token_type")
    var tokenType: String? = null

    @JsonProperty("access_token")
    var accessToken: String? = null

    @get:JsonProperty("expires_in")
    var expiresIn: Int = 0

    @JsonProperty("refresh_token")
    var refreshToken: String? = null

    @field:JsonSetToString(extended = false)
    @JsonProperty("scope")
    var scope: Set<String> = emptySet()

    @JsonProperty("openId")
    var openId: String? = null

    @JsonProperty("unionid")
    var unionId: String? = null

    @JsonProperty("hasBound")
    var hasBound: Boolean = false

    @JsonProperty("message")
    var message: String? = null

    val isOk: Boolean
        get() = message.isNullOrBlank()


    private var additionalInformation: MutableMap<String, Any?> = mutableMapOf()


    init {
        this.message = message
    }

    @JsonAnyGetter
    fun getAdditionalInformation(): MutableMap<String, Any?> {
        return additionalInformation
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

        @JvmStatic
        fun of(message: String): WeixinToken {
            val token = WeixinToken(message)
            return token
        }

    }

}