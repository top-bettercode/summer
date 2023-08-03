package top.bettercode.summer.tools.rapidauth

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class RapidauthSysException(message: String) : IllegalArgumentException("腾讯云号码认证：$message") {
}