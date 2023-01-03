package top.bettercode.summer.tools.amap

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class AmapSysException(message: String) : IllegalArgumentException("高德地图：$message") {
    companion object {
        private const val serialVersionUID = 1L
    }
}