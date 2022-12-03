package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class JsapiSignature(

	@field:JsonProperty("signature")
	val signature: String? = null,

	@field:JsonProperty("appid")
	val appid: String? = null,

	@field:JsonProperty("nonceStr")
	val nonceStr: String? = null,

	@field:JsonProperty("timestamp")
	val timestamp: String? = null
)