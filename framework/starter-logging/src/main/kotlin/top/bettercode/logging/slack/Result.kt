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

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelResult(
        val channel: Channel? = null
) : Result() {
    override fun toString(): String {
        return "ChannelResult(channel=$channel)"
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelsResult(
        val channels: List<Channel>? = null
) : Result() {
    override fun toString(): String {
        return "ChannelsResult(channels=$channels)"
    }
}
