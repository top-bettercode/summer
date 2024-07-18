package top.bettercode.summer.tools.lang.log.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelsResult(
        val channels: List<Channel>? = null
) : SlackResult() {
    override fun toString(): String {
        return "ChannelsResult(channels=$channels)"
    }
}
