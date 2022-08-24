package top.bettercode.summer.util.rapidauth.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class RapidauthRequest(
	/**
	 * App 凭证，sig有效期10分钟
	 */
	@field:JsonProperty("sig")
	val sig: String? = null,

	/**
	 * 开发者服务器当前时间戳（秒）
	 */
	@field:JsonProperty("time")
	val time: String? = null,

	/**
	 * 运营商，移动：mobile， 联通：unicom，电信：telecom
	 */
	@field:JsonProperty("carrier")
	val carrier: String? = null,

	/**
	 * token 有效期为2分钟
	 */
	@field:JsonProperty("token")
	val token: String? = null
)