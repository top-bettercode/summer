package top.bettercode.summer.web.support.gb2260

import com.fasterxml.jackson.annotation.JsonView
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import top.bettercode.summer.security.authorize.Anonymous
import top.bettercode.summer.web.BaseController

@ConditionalOnWebApplication
@Controller
@Anonymous
@RequestMapping(value = ["/divisions"], name = "行政区划")
class GB2260Controller : BaseController() {

    @GetMapping(value = ["/list"], name = "列表（全）")
    fun list(): Any {
        return ok(GB2260.provinces)
    }

    @JsonView(DivisionView::class)
    @GetMapping(value = ["/select"], name = "列表")
    fun select(code: String?): Any {
        return ok(if (code.isNullOrBlank()) GB2260.provinces else GB2260.getDivision(code).children)
    }

}