package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserInfo(

        @field:JsonProperty("nickName")
        var nickName: String? = null,

        @field:JsonProperty("gender")
        var gender: Int? = null,

        @field:JsonProperty("city")
        var city: String? = null,

        @field:JsonProperty("province")
        var province: String? = null,

        @field:JsonProperty("country")
        var country: String? = null,

        @field:JsonProperty("avatarUrl")
        var avatarUrl: String? = null,

        @field:JsonProperty("language")
        var language: String? = null,

        @field:JsonProperty("watermark")
        var watermark: Watermark? = null,

        @field:JsonProperty("is_demote")
        var isDemote: Boolean? = null
)