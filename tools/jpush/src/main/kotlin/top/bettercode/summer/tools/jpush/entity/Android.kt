package top.bettercode.summer.tools.jpush.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Android @JvmOverloads constructor(

    @field:JsonProperty("alert")
    val alert: String? = null,

    @field:JsonProperty("title")
    val title: String? = null,

    @field:JsonProperty("extras")
    val extras: Map<String, Any?>? = null,

    @field:JsonProperty("builder_id")
    val builderId: Int? = null,

    @field:JsonProperty("large_icon")
    val largeIcon: String? = null,

    @field:JsonProperty("intent")
    val intent: Intent? = null
)