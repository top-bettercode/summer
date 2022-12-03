package top.bettercode.summer.tools.jpush.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Intent(

	@field:JsonProperty("url")
	val url: String
)