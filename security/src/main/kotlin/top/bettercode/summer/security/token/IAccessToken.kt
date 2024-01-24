package top.bettercode.summer.security.token

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

/**
 *
 * @author Peter Wu
 */
interface IAccessToken : Serializable {

    @JsonProperty("client_id")
    fun getClientId(): String?

}