package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.weixin.support.WeixinResponse

@JsonIgnoreProperties(ignoreUnknown = true)
data class MsgResult(
        @field:JsonProperty("msgid")
        val msgid: Long? = null
) : WeixinResponse()