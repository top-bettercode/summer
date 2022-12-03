package top.bettercode.summer.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import top.bettercode.summer.logging.annotation.NoRequestLogging
import top.bettercode.summer.logging.annotation.RequestLogging
import top.bettercode.summer.logging.logback.AlarmMarker
import top.bettercode.summer.web.resolver.NoWrapResp

/**
 * @author Peter Wu
 */
@SpringBootApplication
@RestController
@NoWrapResp
@RequestMapping(name = "测试")
class TestController {

    private val log: Logger = LoggerFactory.getLogger(TestController::class.java)

    @NoRequestLogging
    @RequestMapping("/test")
    fun test(@RequestBody request: String?): Any {
        return request ?: "null"
    }

    @RequestMapping("/testNoRead")
    fun testNoRead(request: String?): Any {
        Thread.sleep(5*1000)
        return request ?: "null"
    }

    @RequestMapping("/error/{path}")
    fun error(request: String?): Any {
        val marker = MarkerFactory.getMarker(RequestLoggingFilter.ALARM_LOG_MARKER)
        marker.add(AlarmMarker("initialComment"))
        log.warn(marker, "警告")
        log.error("日志错误", RuntimeException("abc"))
//        log.error("日志错误", RuntimeException("abc"))
//        log.error("日志错误", RuntimeException("abc"))
//        log.error("日志错误", RuntimeException("abc"))
//        log.error("日志错误", RuntimeException("abc"))
//        Thread.sleep(3*1000)
        throw RuntimeException("abc")
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail")
    }

    @RequestLogging(encryptHeaders = ["token"], encryptParameters = ["password"])
    @RequestMapping("/encrypted")
    fun encrypted(): Any {
        return "ok"
    }

    @RequestMapping("/encrypted2")
    fun encrypted2(): Any {
        return "ok"
    }

    @RequestLogging(includeRequestBody = false)
    @RequestMapping("/multipart")
    fun multipart(file: MultipartFile): Any {
        println("------------------:${file.originalFilename}---------------------")
        println("------------------:${file.bytes.size}---------------------")
        return "ok"
    }
}

fun main() {
    SpringApplication.run(TestController::class.java)
}