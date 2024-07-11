package top.bettercode.summer.logging.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelResult(
        val channel: Channel? = null
) : SlackResult() {
    override fun toString(): String {
        return "ChannelResult(channel=$channel)"
    }
}
