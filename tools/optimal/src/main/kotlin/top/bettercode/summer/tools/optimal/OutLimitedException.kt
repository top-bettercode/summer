package top.bettercode.summer.tools.optimal

/**
 *
 * @author Peter Wu
 */
class OutLimitedException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) :
    RuntimeException(message, cause) {
}