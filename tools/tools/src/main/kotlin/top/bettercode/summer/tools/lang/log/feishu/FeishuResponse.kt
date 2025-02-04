package top.bettercode.summer.tools.lang.log.feishu

import com.fasterxml.jackson.annotation.JsonIgnore
import top.bettercode.summer.tools.lang.client.ClientResponse
import top.bettercode.summer.tools.lang.property.PropertiesSource

/**
 * @author Peter Wu
 */
open class FeishuResponse : ClientResponse {
    val code: Int? = null
    val msg: String? = null

    @get:JsonIgnore
    override val message: String?
        get() = if (code == null) msg else errorMsg[code.toString()] ?: msg

    @get:JsonIgnore
    override val isOk: Boolean by lazy { code == 0 }

    fun isInvalidAccessToken(): Boolean {
        return code == 99991663
    }

    override fun toString(): String {
        return "FeishuResult(code=$code, msg=$msg)"
    }

    companion object {
        val errorMsg = PropertiesSource.of("feishu_errors")
    }
}





