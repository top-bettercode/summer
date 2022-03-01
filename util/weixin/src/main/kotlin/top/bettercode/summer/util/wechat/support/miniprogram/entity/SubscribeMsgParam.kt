package top.bettercode.summer.util.wechat.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class SubscribeMsgParam(
    @field:JsonProperty("value")
    val value: String
)