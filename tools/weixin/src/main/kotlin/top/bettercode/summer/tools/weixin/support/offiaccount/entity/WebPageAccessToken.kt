package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.weixin.support.WeixinResponse

@JsonIgnoreProperties(ignoreUnknown = true)
data class WebPageAccessToken(

        @field:JsonProperty("access_token")
        val accessToken: String? = null,

        @field:JsonProperty("expires_in")
        val expiresIn: Int? = null,

        @field:JsonProperty("refresh_token")
        val refreshToken: String? = null,

        @field:JsonProperty("openid")
        val openid: String? = null,

        @field:JsonProperty("scope")
        val scope: String? = null
) : WeixinResponse()