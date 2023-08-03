package top.bettercode.summer.tools.amap

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class AMapSysException(message: String) : IllegalArgumentException("高德地图：$message") {
}