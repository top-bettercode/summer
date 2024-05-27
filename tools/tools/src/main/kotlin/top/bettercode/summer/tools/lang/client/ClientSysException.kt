package top.bettercode.summer.tools.lang.client

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class ClientSysException @JvmOverloads constructor(
    platformName: String,
    marker: String,
    originalMessage: String?,
    cause: Throwable? = null,
    response: Any? = null
) : ClientException(
    platformName = platformName,
    marker = marker,
    originalMessage = originalMessage,
    cause = cause,
    response = response
)