package top.bettercode.summer.tools.weather

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class WeatherSysException(message: String) : IllegalArgumentException("天气数据平台：$message") {
}