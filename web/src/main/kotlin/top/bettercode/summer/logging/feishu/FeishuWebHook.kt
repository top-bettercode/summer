package top.bettercode.summer.logging.feishu

/**
 *
 * @author Peter Wu
 */
data class FeishuWebHook(
    var webhook: String? = null,
    var secret: String? = null
)