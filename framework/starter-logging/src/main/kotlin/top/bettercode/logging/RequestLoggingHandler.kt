package top.bettercode.logging

import org.springframework.web.method.HandlerMethod
import top.bettercode.logging.operation.Operation

/**
 * RequestLogging 处理
 *
 * @author Peter Wu
 */
interface RequestLoggingHandler {

    fun handle(operation: Operation, handler: HandlerMethod?)

}
