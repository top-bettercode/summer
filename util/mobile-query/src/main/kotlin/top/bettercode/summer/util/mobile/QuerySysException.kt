package top.bettercode.summer.util.mobile

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class QuerySysException(message: String) : IllegalArgumentException("号码平台：$message") {
    companion object {
        private const val serialVersionUID = 1L
    }
}