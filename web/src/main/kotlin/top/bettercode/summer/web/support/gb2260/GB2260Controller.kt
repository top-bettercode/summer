package top.bettercode.summer.web.support.gb2260

import com.fasterxml.jackson.annotation.JsonView
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import top.bettercode.summer.logging.annotation.RequestLogging
import top.bettercode.summer.security.authorize.Anonymous
import top.bettercode.summer.web.BaseController

@ConditionalOnWebApplication
@Anonymous
@RequestMapping(value = ["/divisions"], name = "行政区划")
class GB2260Controller : BaseController() {

    @RequestLogging(includeResponseBody = false)
    @JsonView(AllDivisionView::class)
    @GetMapping(value = ["/list"], name = "列表（全）")
    fun list(@RequestParam(defaultValue = "false") vnode: Boolean = false): Any {
        return if (vnode)
            ok(GB2260.provinces)
        else {
            val provinces = GB2260.provinces.map {
                if (it.municipality) {
                    Division(
                        it.code,
                        it.name,
                        it.level,
                        true,
                        it.parentNames,
                        it.children[0].children
                    )
                } else {
                    it
                }
            }
            ok(provinces)
        }
    }

    @RequestLogging(includeResponseBody = false)
    @JsonView(DivisionView::class)
    @GetMapping(value = ["/select"], name = "列表")
    fun select(code: String?, @RequestParam(defaultValue = "false") vnode: Boolean = false): Any {
        return if (code.isNullOrBlank()) {
            ok(GB2260.provinces)
        } else {
            val code1 = String.format("%-6s", code).replace(" ", "0")
            val division = GB2260.getDivision(code1)
            val divisions = division.children
            if (!vnode && division.municipality) {
                ok(divisions[0].children)
            } else {
                ok(divisions)
            }
        }
    }

}