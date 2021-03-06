package top.bettercode.logging.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
open class Result {
    val ok: Boolean? = null
    val error: String? = null
    val ts: String? = null
    override fun toString(): String {
        return "Result(ok=$ok, error=$error, ts=$ts)"
    }
}





