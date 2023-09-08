package top.bettercode.summer.tools.weixin.support.offiaccount.custmsg

import com.fasterxml.jackson.annotation.JsonProperty

class NewsCustMsg @JvmOverloads constructor(

        @field:JsonProperty("touser")
        override val touser: String,

        articles: List<ArticlesItem>,

        @field:JsonProperty("msgtype")
        override val msgtype: String = "news",

        @field:JsonProperty("customservice")
        override val customservice: CustomService? = null,
) : CustMsg(touser, msgtype, customservice) {

    @field:JsonProperty("news")
    val news: News = News(articles)
}

data class News(

        @field:JsonProperty("articles")
        val articles: List<ArticlesItem>
)

data class ArticlesItem @JvmOverloads constructor(

        @field:JsonProperty("title")
        val title: String,

        @field:JsonProperty("picurl")
        val picurl: String,

        @field:JsonProperty("url")
        val url: String,

        @field:JsonProperty("description")
        val description: String = "",

        )
