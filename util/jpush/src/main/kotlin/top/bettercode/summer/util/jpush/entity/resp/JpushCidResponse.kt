package top.bettercode.summer.util.jpush.entity.resp

import com.fasterxml.jackson.annotation.JsonProperty

data class JpushCidResponse(

	@field:JsonProperty("cidlist")
	val cidlist: List<String?>? = null
)