package top.bettercode.summer.util.jpush.entity.resp

import com.fasterxml.jackson.annotation.JsonProperty

data class JpushErrorResponse(

    @field:JsonProperty("error")
    val error: Error? = null
)