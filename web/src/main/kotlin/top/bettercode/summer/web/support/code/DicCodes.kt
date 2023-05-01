package top.bettercode.summer.web.support.code

import org.springframework.util.Assert
import java.io.Serializable

/**
 * @author Peter Wu
 */
class DicCodes //--------------------------------------------
(//--------------------------------------------
        var type: String, var name: String?,
        var codes: Map<Serializable, String>) : Serializable {

    //--------------------------------------------
    fun getName(code: Serializable): String {
        return codes[code] ?: code.toString()
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

    companion object {
        private const val serialVersionUID = 1L
    }
}
