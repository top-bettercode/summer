package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserInfo(

        @field:JsonProperty("nickName")
        val nickName: String? = null,

        @field:JsonProperty("gender")
        val gender: Int? = null,

        @field:JsonProperty("city")
        val city: String? = null,

        @field:JsonProperty("province")
        val province: String? = null,

        @field:JsonProperty("country")
        val country: String? = null,

        @field:JsonProperty("avatarUrl")
        val avatarUrl: String? = null,

        @field:JsonProperty("language")
        val language: String? = null
)