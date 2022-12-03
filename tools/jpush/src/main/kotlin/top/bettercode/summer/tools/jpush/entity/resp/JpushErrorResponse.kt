package top.bettercode.summer.tools.jpush.entity.resp

import com.fasterxml.jackson.annotation.JsonProperty

data class JpushErrorResponse(

    @field:JsonProperty("error")
    val error: Error? = null
)