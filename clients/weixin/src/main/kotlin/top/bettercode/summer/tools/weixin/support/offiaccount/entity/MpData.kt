package top.bettercode.summer.tools.weixin.support.offiaccount.entity

/**
 *
 * @author Peter Wu
 */
class MpData : HashMap<String, Data>() {

    @JvmOverloads
    fun of(key: String, value: String?, color: String? = null): MpData {
        put(key, Data(value ?: "", color))
        return this
    }
}