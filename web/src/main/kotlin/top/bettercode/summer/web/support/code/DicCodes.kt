package top.bettercode.summer.web.support.code

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.util.Assert
import java.io.Serializable

/**
 * @author Peter Wu
 */
data class DicCodes(
    @JsonProperty("type")
    val type: String,
    @JsonProperty("name")
    val name: String?,
    @JsonProperty("codes")
    val codes: Map<Serializable, String>
) : Serializable {

    //--------------------------------------------
    fun getName(code: Serializable): String {
        return codes[code] ?: codes[code.toString()] ?: code.toString()
    }

    fun getCode(name: String): Serializable? {
        Assert.notNull(name, "name不能为空")
        for ((key, value) in codes) {
            if (name == value) {
                return key
            }
        }
        return null
    }

}
