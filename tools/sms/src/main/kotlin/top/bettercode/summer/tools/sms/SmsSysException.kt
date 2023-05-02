package top.bettercode.summer.tools.sms

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class SmsSysException(message: String) : IllegalArgumentException("短信平台：$message") {
    companion object {
        private const val serialVersionUID = 1L
    }
}
