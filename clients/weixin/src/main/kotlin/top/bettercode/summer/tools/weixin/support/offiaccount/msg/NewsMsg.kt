package top.bettercode.summer.tools.weixin.support.offiaccount.msg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "xml")
data class NewsMsg @JvmOverloads constructor(

        @field:JsonProperty("Articles")
        val articles: List<Item>,

        @field:JsonProperty("ToUserName")
        val toUserName: String,

        /**
         * 图文消息个数；当用户发送文本、图片、语音、视频、图文、地理位置这六种消息时，开发者只能回复1条图文消息；其余场景最多可回复8条图文消息
         */
        @field:JsonProperty("ArticleCount")
        val articleCount: Int = 1,

        @field:JsonProperty("FromUserName")
        val fromUserName: String,

        @field:JsonProperty("MsgType")
        val msgType: String = "news",

        @field:JsonProperty("CreateTime")
        val createTime: String = System.currentTimeMillis().toString(),

        )

@JacksonXmlRootElement(localName = "item")
data class Item(

        /**
         *图文消息标题
         */
        @field:JsonProperty("Title")
        val title: String,

        /**
         * 图文消息描述
         */
        @field:JsonProperty("Description")
        val description: String,

        /**
         * 图片链接，支持JPG、PNG格式，较好的效果为大图360*200，小图200*200
         */
        @field:JsonProperty("PicUrl")
        val picUrl: String,

        /**
         * 点击图文消息跳转链接
         */
        @field:JsonProperty("Url")
        val url: String
)


