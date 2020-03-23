package cn.bestwu.api.sign

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.NOT_ACCEPTABLE, reason = "ILLEGAL_SIGN")
class IllegalSignException : RuntimeException("ILLEGAL_SIGN")
