package top.bettercode.summer.util.rapidauth.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class RapidauthResponse(

    /**
     * 手机号码
     */
    @field:JsonProperty("mobile")
    val mobile: String? = null,

    /**
     * 错误信息
     */
    @field:JsonProperty("errmsg")
    val errmsg: String? = null,

    /**
     * 0表示成功，非0表示失败
     */
    @field:JsonProperty("result")
    val result: Int? = null
) {
    fun isOk(): Boolean {
        return result == 0
    }
}