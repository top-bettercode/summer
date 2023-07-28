package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
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