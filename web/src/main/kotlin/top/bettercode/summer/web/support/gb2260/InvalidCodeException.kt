package top.bettercode.summer.web.support.gb2260

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus


@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidCodeException(s: String?) : RuntimeException(s) {
    companion object
}