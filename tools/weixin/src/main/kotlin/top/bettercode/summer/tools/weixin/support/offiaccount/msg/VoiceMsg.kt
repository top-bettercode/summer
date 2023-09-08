package top.bettercode.summer.tools.weixin.support.offiaccount.msg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "xml")
data class VoiceMsg @JvmOverloads constructor(
        @field:JsonProperty("Voice")
        val voice: Media,

        @field:JsonProperty("ToUserName")
        val toUserName: String,

        @field:JsonProperty("FromUserName")
        val fromUserName: String,

        @field:JsonProperty("MsgType")
        val msgType: String = "voice",

        @field:JsonProperty("CreateTime")
        val createTime: String = System.currentTimeMillis().toString(),

        )
