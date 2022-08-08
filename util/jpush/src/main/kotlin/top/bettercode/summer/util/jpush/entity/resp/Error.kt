package top.bettercode.summer.util.jpush.entity.resp

import com.fasterxml.jackson.annotation.JsonProperty

data class Error(

    @field:JsonProperty("code")
    val code: Int? = null,

    @field:JsonProperty("message")
    val message: String? = null
)