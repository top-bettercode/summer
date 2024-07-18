package top.bettercode.summer.tools.lang.log.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
open class SlackResult {
    val ok: Boolean? = null
    val error: String? = null
    val ts: String? = null
    override fun toString(): String {
        return "Result(ok=$ok, error=$error, ts=$ts)"
    }
}





