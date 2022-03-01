package top.bettercode.summer.util.wechat.support

import com.fasterxml.jackson.annotation.JsonIgnore

open class Response(
    val errcode: Int? = null,
    val errmsg: String? = null
) {
    @get:JsonIgnore
    val isOk: Boolean by lazy { errcode == null || errcode == 0 }
}
