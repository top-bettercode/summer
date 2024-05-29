package top.bettercode.summer.web.support.division

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
class DivisionDataController : BaseController() {

    @RequestLogging(includeResponseBody = false)
    @JsonView(AllDivisionView::class)
    @GetMapping(value = ["/list"], name = "列表（全）")
    fun list(@RequestParam(defaultValue = "false") vnode: Boolean = false): Any {
        val divisions = if (vnode)
            DivisionData.provinces
        else {
            val provinces = DivisionData.provinces.map {
                if (it.municipality) {
                    Division(
                            code = it.code,
                            name = it.name,
                            level = it.level,
                            municipality = true,
                            vnode = it.vnode,
                            parentNames = it.parentNames,
                            children = it.children[0].children
                    )
                } else {
                    it
                }
            }
            provinces
        }
        return ok(divisions)
    }

    @RequestLogging(includeResponseBody = false)
    @JsonView(DivisionView::class)
    @GetMapping(value = ["/select"], name = "列表")
    fun select(code: String?, @RequestParam(defaultValue = "false") vnode: Boolean = false): Any {

        val divisions = if (code.isNullOrBlank()) {
            DivisionData.provinces
        } else {
            val code1 = String.format("%-6s", code).replace(" ", "0")
            val division = DivisionData.getDivision(code1)
            val children = division.children
            if (!vnode && division.municipality) {
                children[0].children
            } else {
                children
            }
        }
        return ok(divisions)
    }

}