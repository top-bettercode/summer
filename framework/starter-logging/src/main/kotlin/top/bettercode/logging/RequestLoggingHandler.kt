package top.bettercode.logging

import top.bettercode.logging.operation.Operation
import org.springframework.web.method.HandlerMethod

/**
 * RequestLogging 处理
 *
 * @author Peter Wu
 */
interface RequestLoggingHandler {

    fun handle(operation: Operation, handler: HandlerMethod?)

}
