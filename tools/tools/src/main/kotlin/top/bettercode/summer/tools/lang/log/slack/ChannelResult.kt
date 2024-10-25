package top.bettercode.summer.tools.lang.log.slack


data class ChannelResult(
        val channel: Channel? = null
) : SlackResult() {
    override fun toString(): String {
        return "ChannelResult(channel=$channel)"
    }
}
