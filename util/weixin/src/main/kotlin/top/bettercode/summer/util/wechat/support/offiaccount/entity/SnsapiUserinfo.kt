package top.bettercode.summer.util.wechat.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class SnsapiUserinfo(

	@field:JsonProperty("openid")
	val openid: String? = null,

	@field:JsonProperty("nickname")
	val nickname: String? = null,

	@field:JsonProperty("sex")
	val sex: Int? = null,

	@field:JsonProperty("province")
	val province: String? = null,

	@field:JsonProperty("city")
	val city: String? = null,

	@field:JsonProperty("country")
	val country: String? = null,

	@field:JsonProperty("headimgurl")
	val headimgurl: String? = null,

	@field:JsonProperty("privilege")
	val privilege: List<String?>? = null,

	@field:JsonProperty("unionid")
	val unionid: String? = null
)