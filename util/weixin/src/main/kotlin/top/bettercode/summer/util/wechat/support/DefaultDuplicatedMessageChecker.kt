package top.bettercode.summer.util.wechat.support

import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

class DefaultDuplicatedMessageChecker : DuplicatedMessageChecker {

    override fun isDuplicated(msgKey: String): Boolean {
        return if (cache.getIfPresent(msgKey) == null) {
            cache.put(msgKey, msgKey)
            false
        } else {
            true
        }
    }

    companion object {
        private val cache =
            CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).maximumSize(10000)
                .build<String, String>()
    }
}