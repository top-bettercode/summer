package top.bettercode.summer.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.bettercode.summer.security.authorize.Anonymous
import top.bettercode.summer.security.support.AuthenticationHelper
import top.bettercode.summer.web.BaseController

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@RestController
@ConditionalOnWebApplication
class SecurityTestController : BaseController() {
    @RequestMapping(value = ["/testDefaultAuth"])
    fun test(): Any {
        System.err.println("-----------------------")
        return ok("success")
    }

    @CustAuth
    @EmpAuth
    @RequestMapping(value = ["/testAuth"])
    fun testAuth(): Any {
        val userDetailsOptional = AuthenticationHelper.principal
        System.err.println("-----------------------${userDetailsOptional.orElse(null)}")
        return ok("success")
    }

    @Anonymous
    @RequestMapping(value = ["/testNoAuth"])
    fun testNoAuth(): Any {
        System.err.println("-----------------------")
        return ok("success")
    }
}
