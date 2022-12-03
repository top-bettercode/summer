package top.bettercode.summer.tools.qvod

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import top.bettercode.summer.web.BaseController
import javax.validation.constraints.NotBlank

@Validated
@RestController
@ConditionalOnWebApplication
@RequestMapping(value = ["/qvod"], name = "腾讯云")
class QvodController(
    private val qvodClient: QvodClient
) : BaseController() {

    @ResponseBody
    @GetMapping(value = ["/signature"], name = "客户端上传签名")
    fun signature(): Any {
        return ok(qvodClient.signature())
    }

    @ResponseBody
    @PostMapping(value = ["/antiLeechUrl"], name = "防盗链地址获取")
    fun antiLeechUrl(@NotBlank url: String): Any {
        return ok(qvodClient.antiLeechUrl(url))
    }
}