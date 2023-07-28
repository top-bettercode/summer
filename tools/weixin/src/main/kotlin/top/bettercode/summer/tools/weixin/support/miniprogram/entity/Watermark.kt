package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Watermark(

        @field:JsonProperty("timestamp")
        val timestamp: Int? = null,

        @field:JsonProperty("appid")
        val appid: String? = null
)