package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.weixin.support.WeixinResponse

data class PhoneInfoResp(
        @field:JsonProperty("phone_info")
        var phoneInfo: PhoneInfo? = null
) : WeixinResponse()