package top.bettercode.summer.util.qvod

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import top.bettercode.simpleframework.web.BaseController

@ConditionalOnWebApplication
@RequestMapping(value = ["/qvod"], name = "腾讯云")
class QvodController(
    private val qvodClient: QvodClient
) : BaseController() {

    @ResponseBody
    @GetMapping(value = ["/signature"], name = "客户端上传签名")
    fun signature(isPicture: Boolean = false): Any {
        return ok(qvodClient.signature(isPicture))
    }

}