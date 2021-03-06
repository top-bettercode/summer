package top.bettercode.summer.util.wechat.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class PhoneInfo(

	@field:JsonProperty("phoneNumber")
	val phoneNumber: String? = null,

	@field:JsonProperty("purePhoneNumber")
	val purePhoneNumber: String? = null,

	@field:JsonProperty("countryCode")
	val countryCode: Int? = null,

	@field:JsonProperty("watermark")
	val watermark: Watermark? = null
)