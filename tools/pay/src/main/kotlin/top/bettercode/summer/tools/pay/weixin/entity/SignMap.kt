package top.bettercode.summer.tools.pay.weixin.entity

/**
 *
 * @author Peter Wu
 */
class SignMap : HashMap<String, Any?>() {
    var sign: String?
        get() = this["sign"] as String?
        set(value) {
            this["sign"] = value
        }
}