package top.bettercode.summer.util.wechat.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class TemplateMsgRequest @JvmOverloads constructor(

    @field:JsonProperty("touser")
    val touser: String,

    @field:JsonProperty("template_id")
    val templateId: String,

    @field:JsonProperty("data")
    val data: Map<String, Data>,

    @field:JsonProperty("url")
    val url: String? = null,

    @field:JsonProperty("miniprogram")
    val miniprogram: Miniprogram? = null
)