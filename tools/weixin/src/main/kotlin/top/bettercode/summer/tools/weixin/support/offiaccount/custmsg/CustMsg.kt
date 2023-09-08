package top.bettercode.summer.tools.weixin.support.offiaccount.custmsg

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @author Peter Wu
 */
open class CustMsg @JvmOverloads constructor(

        @field:JsonProperty("touser")
        open val touser: String,

        @field:JsonProperty("msgtype")
        open val msgtype: String,

        @field:JsonProperty("customservice")
        open val customservice: CustomService? = null,
)

data class CustomService(

        @field:JsonProperty("kf_account")
        val kfAccount: String
)
