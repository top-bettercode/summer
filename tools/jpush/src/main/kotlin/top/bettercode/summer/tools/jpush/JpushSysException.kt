package top.bettercode.summer.tools.jpush

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class JpushSysException(val error: top.bettercode.summer.tools.jpush.entity.resp.Error) :
    IllegalArgumentException("极光推送平台：${error.message}") {
    companion object {
        private const val serialVersionUID = 1L
    }
}