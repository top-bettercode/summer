package cn.bestwu.loggingtest

import cn.bestwu.logging.RequestLoggingFilter
import cn.bestwu.logging.annotation.NoRequestLogging
import cn.bestwu.logging.annotation.RequestLogging
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * @author Peter Wu
 */
@SpringBootApplication
@RestController
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
        return request ?: "null"
    }

    @RequestMapping("/errorMail/{path}")
    fun error(request: String?): Any {
        log.error("日志错误", RuntimeException("abc"))
        log.error("日志错误", RuntimeException("abc"))
        log.error("日志错误", RuntimeException("abc"))
        log.error("日志错误", RuntimeException("abc"))
        log.error("日志错误", RuntimeException("abc"))
        log.warn(MarkerFactory.getMarker(RequestLoggingFilter.ALARM_LOG_MARKER), "警告")
//        Thread.sleep(3*1000)
//        throw RuntimeException("abc")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail")
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

    @RequestMapping("/multipart")
    fun multipart(file: MultipartFile): Any {
        println("------------------:${file.originalFilename}---------------------")
        println("------------------:${file.bytes.size}---------------------")
        return "ok"
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(TestController::class.java)
}