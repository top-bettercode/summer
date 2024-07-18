package top.bettercode.summer.tools.lang.log.feishu

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
open class FeishuResult {
    val code: Int? = null
    val msg: String? = null


    fun isOk(): Boolean {
        return code == 0
    }

    fun isInvalidAccessToken(): Boolean {
        return code == 99991663
    }

    override fun toString(): String {
        return "FeishuResult(code=$code, msg=$msg)"
    }
}





