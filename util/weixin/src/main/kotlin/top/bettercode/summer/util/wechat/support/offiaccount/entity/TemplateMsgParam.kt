package top.bettercode.summer.util.wechat.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty


data class TemplateMsgParam @JvmOverloads constructor(

    @field:JsonProperty("value")
    val value: String,

    @field:JsonProperty("color")
    val color: String? = null
)