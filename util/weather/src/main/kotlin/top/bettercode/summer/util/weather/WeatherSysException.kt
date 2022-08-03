package top.bettercode.summer.util.weather

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class WeatherSysException(message: String) : IllegalArgumentException("天气数据平台：$message") {
    companion object {
        private const val serialVersionUID = 1L
    }
}