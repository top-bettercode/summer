package top.bettercode.summer.tools.weixin.support

import top.bettercode.summer.tools.weixin.support.offiaccount.entity.CachedValue

/**
 *
 * @author Peter Wu
 */
interface IWeixinCache {

    fun put(key: String, value: CachedValue)
    fun get(key: String): CachedValue?
    fun evict(key: String)
    fun clear()

}