package top.bettercode.summer.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * 响应客户端
 *
 * @author Peter Wu
 */
open class Response {
    /**
     * 成功创建资源
     *
     * @param resource resource
     * @return 201 ResponseEntity
     */
    protected fun created(resource: Any): ResponseEntity<*> {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resource)
    }

    /**
     * 成功更新资源
     *
     * @param resource resource
     * @return 200 ResponseEntity
     */
    protected fun updated(resource: Any?): ResponseEntity<*> {
        return ok(resource)
    }

    protected open fun of(`object`: Any): RespExtra<*>? {
        return RespExtra(`object`)
    }

    /**
     * @param object object
     * @return 200 ResponseEntity
     */
    protected open fun ok(`object`: Any?): ResponseEntity<*> {
        return ResponseEntity.ok().body(`object`)
    }

    /**
     * @param message message
     * @return 200 ResponseEntity
     */
    protected fun message(message: String?): ResponseEntity<*> {
        return ok(RespEntity<Any>(HttpStatus.OK.value().toString(), message))
    }

    protected fun message(status: String, message: String?): ResponseEntity<*> {
        return ok(RespEntity<Any>(status, message))
    }

    /**
     * @param message message
     * @return 400 ResponseEntity
     */
    protected fun errorMessage(message: String?): ResponseEntity<*> {
        return ok(RespEntity<Any>(HttpStatus.BAD_REQUEST.value().toString(), message))
    }

    /**
     * 响应空白内容
     *
     * @return 204
     */
    protected fun noContent(): ResponseEntity<*> {
        return ResponseEntity.noContent().build<Any>()
    }
}
