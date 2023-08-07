package top.bettercode.summer.web.error

import top.bettercode.summer.web.IRespEntity
import top.bettercode.summer.web.RespEntity

/**
 * @author Peter Wu
 */
interface IRespEntityConverter {
    fun convert(respEntity: RespEntity<*>): IRespEntity
}
