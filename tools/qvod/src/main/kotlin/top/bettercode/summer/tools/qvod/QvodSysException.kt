package top.bettercode.summer.tools.qvod

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class QvodSysException(message: String) : IllegalArgumentException("腾讯云点播：$message") {
    companion object {
        private const val serialVersionUID = 1L
    }
}