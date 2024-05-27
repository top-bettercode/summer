package top.bettercode.summer.tools.lang.client

/**
 *
 * @author Peter Wu
 */
interface ApiExceptions {
    val platformName: String
    val marker: String

    fun clientException(
    ): ClientException {
        return clientException(message = null, response = null)
    }

    fun clientException(
        cause: Throwable?,
    ): ClientException {
        return clientException(cause = cause, response = null)
    }

    fun clientException(
        cause: Throwable?,
        response: Any?
    ): ClientException {
        return ClientException(
            platformName = platformName,
            marker = marker,
            originalMessage = if (cause is ClientException) cause.originalMessage else cause?.message,
            cause = cause,
            response = response
        )

    }

    fun clientException(
        message: String?
    ): ClientException {
        return clientException(message = message, response = null)
    }

    fun clientException(
        message: String?, response: Any?
    ): ClientException {
        return ClientException(
            platformName = platformName,
            marker = marker,
            originalMessage = message,
            cause = null,
            response = response
        )

    }

    fun clientSysException(
    ): ClientException {
        return clientSysException(message = null, response = null)
    }

    fun clientSysException(
        cause: Throwable?
    ): ClientException {
        return clientSysException(cause = cause, response = null)
    }

    fun clientSysException(
        cause: Throwable?,
        response: Any?
    ): ClientSysException {
        return ClientSysException(
            platformName = platformName,
            marker = marker,
            originalMessage = if (cause is ClientException) cause.originalMessage else cause?.message,
            cause = cause,
            response = response
        )
    }

    fun clientSysException(
        message: String?,
    ): ClientSysException {
        return clientSysException(message = message, response = null)
    }

    fun clientSysException(
        message: String?,
        response: Any?
    ): ClientSysException {
        return ClientSysException(
            platformName = platformName,
            marker = marker,
            originalMessage = message,
            response = response
        )
    }

}