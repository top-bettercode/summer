package top.bettercode.summer.tools.weixin.support

import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit

class DefaultDuplicatedMessageChecker : IDuplicatedMessageChecker {

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
                Caffeine.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).maximumSize(10000)
                        .build<String, String>()
    }
}