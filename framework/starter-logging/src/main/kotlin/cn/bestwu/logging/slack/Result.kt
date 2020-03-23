package cn.bestwu.logging.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Result(
        val ok: Boolean? = null,
        val error: String? = null,
        val ts: String? = null,
        val channels: List<Channel>? = null,
        val channel: Channel? = null
)
