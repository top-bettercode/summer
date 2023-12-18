package top.bettercode.summer.tools.weixin.support.offiaccount.custmsg

import com.fasterxml.jackson.annotation.JsonProperty

class MpNewsArticleCustMsg @JvmOverloads constructor(

        @field:JsonProperty("touser")
        override val touser: String,

        articleId: String,

        @field:JsonProperty("msgtype")
        override val msgtype: String = "mpnewsarticle",

        @field:JsonProperty("customservice")
        override val customservice: CustomService? = null,
) : CustMsg(touser, msgtype, customservice) {

    @field:JsonProperty("mpnewsarticle")
    val mpnewsarticle: Mpnewsarticle = Mpnewsarticle(articleId)
}

data class Mpnewsarticle(

        @field:JsonProperty("article_id")
        val articleId: String
)
