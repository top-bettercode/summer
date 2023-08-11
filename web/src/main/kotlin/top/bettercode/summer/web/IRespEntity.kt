package top.bettercode.summer.web

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus

/**
 * 响应实体
 *
 * @author Peter Wu
 */
interface IRespEntity {
    @JvmDefault
    @get:JsonIgnore
    val httpStatusCode: Int?
        get() = HttpStatus.OK.value()

    @JsonIgnore
    fun toMap(): Map<String?, Any?>
}
