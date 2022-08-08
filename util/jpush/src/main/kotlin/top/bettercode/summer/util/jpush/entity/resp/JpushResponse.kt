package top.bettercode.summer.util.jpush.entity.resp

import com.fasterxml.jackson.annotation.JsonProperty

data class JpushResponse(

	@field:JsonProperty("sendno")
	val sendno: String? = null,

	@field:JsonProperty("msg_id")
	val msgId: String? = null
)