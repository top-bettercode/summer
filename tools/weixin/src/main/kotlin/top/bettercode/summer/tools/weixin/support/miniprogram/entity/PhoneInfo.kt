package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class PhoneInfo(

        @field:JsonProperty("phoneNumber")
        var phoneNumber: String? = null,

        @field:JsonProperty("purePhoneNumber")
        var purePhoneNumber: String? = null,

        @field:JsonProperty("countryCode")
        var countryCode: String? = null,

        @field:JsonProperty("watermark")
        var watermark: Watermark? = null
)