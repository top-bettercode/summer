package top.bettercode.summer.tools.jpush.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Ios @JvmOverloads constructor(

        @field:JsonProperty("alert")
        val alert: String? = null,

        @field:JsonProperty("extras")
        val extras: Map<String, Any?>? = null,

        @field:JsonProperty("sound")
        val sound: String? = null,

        @field:JsonProperty("badge")
        val badge: String = "+1",

        @field:JsonProperty("thread-id")
        val threadId: String? = null
)