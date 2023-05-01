package top.bettercode.summer.web.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * 找不到资源
 *
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class ResourceNotFoundException @JvmOverloads constructor(message: String? = "resource.not.found", cause: Throwable? = null) : RuntimeException(message, cause) {
    companion object {
        private const val serialVersionUID = 1L
    }
}