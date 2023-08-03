package top.bettercode.summer.tools.mobile

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class QuerySysException(message: String) : IllegalArgumentException("号码平台：$message") {
}