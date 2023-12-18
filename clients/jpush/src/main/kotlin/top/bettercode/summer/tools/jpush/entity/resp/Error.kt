package top.bettercode.summer.tools.jpush.entity.resp

import com.fasterxml.jackson.annotation.JsonProperty

data class Error(

        @field:JsonProperty("code")
        val code: Int? = null,

        @field:JsonProperty("message")
        val message: String? = null
)