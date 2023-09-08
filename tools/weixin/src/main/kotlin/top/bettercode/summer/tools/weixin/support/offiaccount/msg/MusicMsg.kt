package top.bettercode.summer.tools.weixin.support.offiaccount.msg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "xml")
data class MusicMsg @JvmOverloads constructor(

        @field:JsonProperty("Music")
        val music: Music,

        @field:JsonProperty("ToUserName")
        val toUserName: String,

        @field:JsonProperty("FromUserName")
        val fromUserName: String,

        @field:JsonProperty("MsgType")
        val msgType: String = "music",

        @field:JsonProperty("CreateTime")
        val createTime: Long = System.currentTimeMillis() / 1000,

        )

data class Music @JvmOverloads constructor(
        @field:JsonProperty("ThumbMediaId")
        val thumbMediaId: String,

        @field:JsonProperty("Title")
        val title: String? = null,

        @field:JsonProperty("MusicUrl")
        val musicUrl: String? = null,

        @field:JsonProperty("HQMusicUrl")
        val hQMusicUrl: String? = null,

        @field:JsonProperty("Description")
        val description: String? = null,

        )
