package top.bettercode.summer.tools.weixin.support.offiaccount.custmsg

import com.fasterxml.jackson.annotation.JsonProperty

data class CardCustMsg @JvmOverloads constructor(
        @field:JsonProperty("touser")
        override val touser: String,

        val cardId: String,

        @field:JsonProperty("customservice")
        override val msgtype: String = "wxcard",

        @field:JsonProperty("customservice")
        override val customservice: CustomService? = null,
) : CustMsg(touser, msgtype, customservice) {
    @field:JsonProperty("wxcard")
    val wxcard: Wxcard = Wxcard(cardId)
}

data class Wxcard(
        @field:JsonProperty("card_id")
        val cardId: String
)
