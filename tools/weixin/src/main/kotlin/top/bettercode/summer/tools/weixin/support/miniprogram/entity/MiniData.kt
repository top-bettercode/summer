package top.bettercode.summer.tools.weixin.support.miniprogram.entity

/**
 *
 * @author Peter Wu
 */
class MiniData : HashMap<String, Data>() {

    fun of(key: String, value: String?): MiniData {
        put(key, Data(value ?: ""))
        return this
    }

}