package top.bettercode.summer.web.error

import org.springframework.web.context.request.RequestAttributes
import top.bettercode.summer.web.IRespEntity
import top.bettercode.summer.web.RespEntity

/**
 * @author Peter Wu
 */
interface IErrorRespEntityHandler {
    fun handle(requestAttributes: RequestAttributes?, respEntity: RespEntity<Any>?): IRespEntity
}
