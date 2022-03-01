package top.bettercode.summer.util.wechat.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.util.wechat.support.WeixinResponse

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