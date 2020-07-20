package cn.bestwu.logging.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
open class Result {
    val ok: Boolean? = null
    val error: String? = null
    val ts: String? = null
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelResult(
        val channel: Channel? = null
) : Result()

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelsResult(
        val channels: List<Channel>? = null
) : Result()
