package top.bettercode.summer.util.wechat.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.util.wechat.support.WeixinResponse

data class MsgResult(
    @field:JsonProperty("msgid")
    val msgid: Long? = null
) : WeixinResponse()