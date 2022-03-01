package top.bettercode.summer.util.wechat.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty
import javax.annotation.Generated
import java.io.Serializable

data class Miniprogram(

	@field:JsonProperty("appid")
	val appid: String,

	@field:JsonProperty("pagepath")
	val pagepath: String? = null
)