package top.bettercode.summer.logging

import org.springframework.web.method.HandlerMethod
import top.bettercode.summer.tools.lang.operation.Operation

/**
 * RequestLogging 处理
 *
 * @author Peter Wu
 */
interface RequestLoggingHandler {

    fun handle(operation: Operation, handler: HandlerMethod?)

}
