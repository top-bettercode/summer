package top.bettercode.summer.util.wechat.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.util.wechat.support.WeixinResponse

data class PhoneInfoResp(
	@field:JsonProperty("phone_info")
	val phoneInfo: PhoneInfo? = null
): WeixinResponse()