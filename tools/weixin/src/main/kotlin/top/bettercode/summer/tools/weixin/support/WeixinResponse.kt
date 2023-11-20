package top.bettercode.summer.tools.weixin.support

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
open class WeixinResponse(
        var errcode: Int? = null,
        var errmsg: String? = null
) {
    @get:JsonIgnore
    val isOk: Boolean by lazy { errcode == null || errcode == 0 }
}
