package top.bettercode.summer.tools.weixin.support

import top.bettercode.summer.tools.weixin.properties.IWexinProperties

/**
 *
 * @author Peter Wu
 */
class WeixinToken() : HashMap<String, Any?>() {

    init {
        this["access_token"] = ""
        this["expires_in"] = 0
        this["refresh_token"] = ""
        this[IWexinProperties.OPEN_ID_NAME] = ""
        this["scope"] = ""
        this["unionid"] = ""
        this["hasBound"] = false
        this["isOk"] = true
        this["message"] = ""
    }

    var accessToken: String
        get() = get("access_token") as String
        set(value) {
            put("access_token", value)
        }

    var expiresIn: Int?
        get() = get("expires_in") as Int?
        set(value) {
            put("expires_in", value)
        }
    var refreshToken: String?
        get() = get("refresh_token") as String?
        set(value) {
            put("refresh_token", value)
        }
    var openId: String
        get() = get(IWexinProperties.OPEN_ID_NAME) as String
        set(value) {
            put(IWexinProperties.OPEN_ID_NAME, value)
        }
    var scope: String?
        get() = get("scope") as String?
        set(value) {
            put("scope", value)
        }
    var unionId: String
        get() = get("unionid") as String
        set(value) {
            put("unionid", value)
        }
    var hasBound: Boolean
        get() = get("hasBound") as Boolean
        set(value) {
            put("hasBound", value)
        }

    var message: String
        get() = get("message") as String
        set(value) {
            put("message", value)
            if (value.isNotBlank())
                put("isOk", false)
        }


    constructor(message: String?) : this() {
        this.message = message ?: ""
    }

    companion object
}