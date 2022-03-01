package top.bettercode.summer.util.wechat.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty
import javax.annotation.Generated
import java.io.Serializable

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