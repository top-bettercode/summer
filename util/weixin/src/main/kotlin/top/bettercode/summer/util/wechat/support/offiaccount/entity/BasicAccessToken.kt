package top.bettercode.summer.util.wechat.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.util.wechat.support.WeixinResponse

data class BasicAccessToken(

	@field:JsonProperty("access_token")
	val accessToken: String? = null,

	@field:JsonProperty("expires_in")
	val expiresIn: Int? = null
): WeixinResponse()