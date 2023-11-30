package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime

/**
 *
 * @author Peter Wu
 */
data class CachedValue(val value: String, val expiresIn: Duration, val expiresTime: LocalDateTime = LocalDateTime.now().plus(expiresIn)) : Serializable {

    fun expired(): Boolean {
        return !expiresTime.isBefore(LocalDateTime.now())
    }
}