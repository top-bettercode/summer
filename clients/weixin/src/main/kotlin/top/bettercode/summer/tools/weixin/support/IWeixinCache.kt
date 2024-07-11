package top.bettercode.summer.tools.weixin.support

import top.bettercode.summer.tools.lang.ExpiringValue

/**
 *
 * @author Peter Wu
 */
interface IWeixinCache {

    fun put(key: String, value: ExpiringValue<String>)
    fun get(key: String): ExpiringValue<String>?
    fun evict(key: String)
    fun clear()

}