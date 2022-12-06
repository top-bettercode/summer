package top.bettercode.summer.tools.jpush.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Message @JvmOverloads constructor(

    @field:JsonProperty("msg_content")
    val msgContent: String,

    @field:JsonProperty("extras")
    val extras: Map<String, Any?>? = null,

    @field:JsonProperty("content_type")
    val contentType: String? = null,

    @field:JsonProperty("title")
    val title: String? = null
)