package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.weixin.support.WeixinResponse

@JsonIgnoreProperties(ignoreUnknown = true)
data class WebPageAccessToken @JvmOverloads constructor(

        @field:JsonProperty("access_token")
        var accessToken: String? = null,

        @field:JsonProperty("expires_in")
        var expiresIn: Int? = null,

        @field:JsonProperty("refresh_token")
        var refreshToken: String? = null,

        @field:JsonProperty("openid")
        var openid: String? = null,

        @field:JsonProperty("scope")
        var scope: String? = null
) : WeixinResponse()