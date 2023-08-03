package top.bettercode.summer.web

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.annotation.JsonView
import java.io.Serializable

/**
 * @author Peter Wu
 */
class RespExtra<T>(@get:JsonView(Any::class)
                   @get:JsonUnwrapped val content: T) : Serializable {
    private var extra: MutableMap<String, Any> = HashMap()

    @get:JsonUnwrapped
    @get:JsonView(Any::class)
    var extraPOJO: Any? = null

    fun extra(key: String, value: Any): RespExtra<T> {
        extra[key] = value
        return this
    }

    fun extraPOJO(any: Any?): RespExtra<T> {
        extraPOJO = any
        return this
    }

    @JsonAnyGetter
    fun getExtra(): Map<String, Any> {
        return extra
    }

    fun setExtra(extra: MutableMap<String, Any>) {
        this.extra = extra
    }

}
