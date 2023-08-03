package top.bettercode.summer.web.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
class AccessDeniedException @JvmOverloads constructor(message: String? = "access.denied") : RuntimeException(message)
