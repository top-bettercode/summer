package top.bettercode.summer.util.wechat.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Miniprogram  @JvmOverloads constructor(

	@field:JsonProperty("appid")
	val appid: String,

	@field:JsonProperty("pagepath")
	val pagepath: String? = null
)