package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.weixin.support.WeixinResponse

@JsonIgnoreProperties(ignoreUnknown = true)
data class PhoneInfoResp(
    @field:JsonProperty("phone_info")
    val phoneInfo: PhoneInfo? = null
) : WeixinResponse()