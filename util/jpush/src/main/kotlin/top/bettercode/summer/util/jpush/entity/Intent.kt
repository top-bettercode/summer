package top.bettercode.summer.util.jpush.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Intent(

	@field:JsonProperty("url")
	val url: String
)