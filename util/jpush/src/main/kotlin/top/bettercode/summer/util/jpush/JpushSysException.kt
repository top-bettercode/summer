package top.bettercode.summer.util.jpush

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import top.bettercode.summer.util.jpush.entity.resp.JpushErrorResponse

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class JpushSysException(val error: JpushErrorResponse) : IllegalArgumentException("极光推送平台：${error.error?.message}") {
    companion object {
        private const val serialVersionUID = 1L
    }
}