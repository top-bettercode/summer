package top.bettercode.summer.tools.sap.connection.pojo

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * @author Peter Wu
 */
interface ISapReturn {
    @get:JsonIgnore
    val isOk: Boolean
        get() = isSuccess

    @get:JsonIgnore
    val isSuccess: Boolean

    @get:JsonIgnore
    val message: String?
}
