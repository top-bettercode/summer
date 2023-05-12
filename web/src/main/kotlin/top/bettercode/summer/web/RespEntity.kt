package top.bettercode.summer.web

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import org.springframework.http.HttpStatus
import top.bettercode.summer.web.exception.SystemException
import java.io.Serializable

/**
 * @author Peter Wu
 */
class RespEntity<T> : IRespEntity, Serializable {
    override var httpStatusCode: Int? = null

    @JvmField
    @JsonView(Any::class)
    @JsonProperty(KEY_STATUS)
    var status: String? = null

    @JvmField
    @JsonProperty(KEY_MESSAGE)
    @JsonView(Any::class)
    var message: String? = null

    @JvmField
    @JsonProperty(KEY_TRACE)
    @JsonView(Any::class)
    var trace: String? = null

    @JvmField
    @JsonProperty(KEY_ERRORS)
    @JsonView(Any::class)
    var errors: Any? = null

    @JsonProperty(KEY_DATA)
    @JsonView(Any::class)
    var data: T? = null
        private set

    constructor()
    constructor(status: String, message: String?) {
        this.status = status
        this.message = message
    }

    constructor(data: T) {
        status = HttpStatus.OK.value().toString()
        message = ""
        this.data = data
    }

    fun setHttpStatusCode(httpStatusCode: Int) {
        this.httpStatusCode = httpStatusCode
        status = httpStatusCode.toString()
    }

    fun setData(data: T) {
        this.data = data
    }

    @get:JsonIgnore
    val isOk: Boolean
        //--------------------------------------------
        get() = HttpStatus.OK.value().toString() == status

    override fun toMap(): Map<String?, Any?> {
        val map = RespEntityMap()
        map[KEY_STATUS] = status
        map[KEY_MESSAGE] = message
        map[KEY_DATA] = data
        map[KEY_TRACE] = trace
        map[KEY_ERRORS] = errors
        return map
    }

    class RespEntityMap : HashMap<String?, Any?>(), IRespEntity {
        override val httpStatusCode: Int?
            get() = try {
                HttpStatus.valueOf((this[KEY_STATUS] as String?)!!.toInt()).value()
            } catch (e: Exception) {
                HttpStatus.OK.value()
            }


        override fun toMap(): Map<String?, Any?> {
            return this
        }

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    companion object {
        private const val serialVersionUID = 1L
        const val KEY_STATUS = "status"
        const val KEY_MESSAGE = "message"
        const val KEY_DATA = "data"
        const val KEY_TRACE = "trace"
        const val KEY_ERRORS = "errors"
        fun assertOk(respEntity: RespEntity<*>) {
            if (!respEntity.isOk) {
                throw SystemException(respEntity.status!!, respEntity.message)
            }
        }

        fun assertOk(respEntity: RespEntity<*>, message: String?) {
            if (!respEntity.isOk) {
                throw SystemException(respEntity.status!!, message)
            }
        }

        @JsonIgnore
        fun <T> of(status: String, message: String?): RespEntity<T> {
            return RespEntity(status, message)
        }

        @JvmStatic
        @JsonIgnore
        fun <T> ok(): RespEntity<T?> {
            return RespEntity(null)
        }

        @JvmStatic
        @JsonIgnore
        fun <T> ok(data: T): RespEntity<T> {
            return RespEntity(data)
        }

        @JsonIgnore
        fun <T> fail(): RespEntity<T> {
            return fail("")
        }

        @JsonIgnore
        fun <T> fail(message: String?): RespEntity<T> {
            return fail(message, null)
        }

        @JsonIgnore
        fun <T> fail(message: String?, errors: Any?): RespEntity<T> {
            return fail(HttpStatus.INTERNAL_SERVER_ERROR.value().toString(), message, errors)
        }

        @JsonIgnore
        fun <T> fail(status: String, message: String?, errors: Any?): RespEntity<T> {
            val respEntity = RespEntity<T>(status, message)
            respEntity.errors = errors
            return respEntity
        }
    }
}
