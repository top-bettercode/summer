package top.bettercode.summer.tools.weixin.support.offiaccount.msg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "xml")
data class TextMsg @JvmOverloads constructor(

        @field:JsonProperty("Content")
        val content: String,

        @field:JsonProperty("ToUserName")
        val toUserName: String,

        @field:JsonProperty("FromUserName")
        val fromUserName: String,

        @field:JsonProperty("MsgType")
        val msgType: String = "text",

        @field:JsonProperty("CreateTime")
        val createTime: String = System.currentTimeMillis().toString(),

        )
