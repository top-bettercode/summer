package top.bettercode.summer.tools.sms.b2m

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @author Peter Wu
 */
data class B2mBalance(
        @field:JsonProperty("balance")
        val balance: Long? = null)