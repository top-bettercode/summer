package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.weixin.support.WeixinResponse

data class MsgResult(
        @field:JsonProperty("msgid")
        var msgid: Long? = null
) : WeixinResponse()