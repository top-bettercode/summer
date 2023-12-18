package top.bettercode.summer.tools.weixin.support.offiaccount.msg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "xml")
data class VideoMsg @JvmOverloads constructor(

    @field:JsonProperty("Video")
    val video: Media,

    @field:JsonProperty("ToUserName")
    val toUserName: String,

    @field:JsonProperty("FromUserName")
    val fromUserName: String,

    @field:JsonProperty("MsgType")
    val msgType: String = "video",

    @field:JsonProperty("CreateTime")
    val createTime: String = System.currentTimeMillis().toString(),

    )
