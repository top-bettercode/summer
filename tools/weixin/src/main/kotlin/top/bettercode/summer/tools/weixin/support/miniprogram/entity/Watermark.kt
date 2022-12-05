package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Watermark(

    @field:JsonProperty("timestamp")
    val timestamp: Int? = null,

    @field:JsonProperty("appid")
    val appid: String? = null
)