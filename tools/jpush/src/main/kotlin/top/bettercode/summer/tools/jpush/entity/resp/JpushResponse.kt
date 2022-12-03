package top.bettercode.summer.tools.jpush.entity.resp

import com.fasterxml.jackson.annotation.JsonProperty

data class JpushResponse(

	@field:JsonProperty("sendno")
	val sendno: String? = null,

	@field:JsonProperty("msg_id")
	val msgId: String? = null
)