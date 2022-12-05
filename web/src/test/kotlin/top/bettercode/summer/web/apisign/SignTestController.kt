package top.bettercode.api.sign

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.bettercode.summer.web.apisign.ApiSignIgnore
import javax.servlet.http.HttpServletRequest

/**
 * @author Peter Wu
 * @since 1.1.4
 */
@SpringBootApplication
@RestController
class SignTestController {

    @RequestMapping("/testSign")
    fun test(httpServletRequest: HttpServletRequest): String {
        httpServletRequest.parameterMap.forEach { (t, u) ->
            System.err.println("$t:${u.joinToString()}")
        }
        return "success"
    }

}

@SpringBootApplication
@RestController
@ApiSignIgnore
class ApiSignIgnoreTestController {

    @RequestMapping("/apiSignIgnore/type")
    fun test(): String {
        return "success"
    }

}

@SpringBootApplication
@RestController
class ApiSignIgnoreMethodController {

    @ApiSignIgnore
    @RequestMapping("/apiSignIgnore/method")
    fun test(): String {
        return "success"
    }

}
