package top.bettercode.summer.tools.lang.client

/**
 * @author Peter Wu
 */
open class ClientException @JvmOverloads constructor(
    /**
     * 平台名称
     */
    val platformName: String,
    /**
     * 标记
     */
    val marker: String,
    /**
     * The original message.
     */
    val originalMessage: String? = "请求失败",
    cause: Throwable? = null,
    val isTimeout: Boolean = false,
    val response: Any? = if (cause is ClientException) cause.response else null
) : RuntimeException(
    "${platformName}：${originalMessage ?: (if (cause is ClientException) cause.originalMessage else cause?.message) ?: "请求失败"}",
    cause
)