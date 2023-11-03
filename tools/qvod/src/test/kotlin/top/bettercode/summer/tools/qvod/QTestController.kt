package top.bettercode.summer.tools.qvod

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.bettercode.summer.web.BaseController
import top.bettercode.summer.web.form.FormDuplicateCheck

/**
 * @author Peter Wu
 */
@RestController
@Validated
class QTestController : BaseController() {
    @FormDuplicateCheck
    @RequestMapping(value = ["/test"])
    fun test(): Any {
        return ok(DataBean())
    }
}