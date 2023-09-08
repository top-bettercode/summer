package top.bettercode.summer.tools.weixin.support.offiaccount.msg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "xml")
class ImageMsg @JvmOverloads constructor(
        mediaId: String,
        @field:JsonProperty("ToUserName")
        val toUserName: String,

        @field:JsonProperty("FromUserName")
        val fromUserName: String,

        @field:JsonProperty("MsgType")
        val msgType: String = "image",

        @field:JsonProperty("CreateTime")
        val createTime: String = System.currentTimeMillis().toString(),
        ) {

    @field:JsonProperty("Image")
    val image: Media = Media(mediaId)
}

