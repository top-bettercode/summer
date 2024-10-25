package top.bettercode.summer.tools.lang.log.slack


data class ChannelsResult(
        val channels: List<Channel>? = null
) : SlackResult() {
    override fun toString(): String {
        return "ChannelsResult(channels=$channels)"
    }
}
