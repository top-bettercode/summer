package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import java.io.Serializable
import java.time.LocalDateTime

/**
 *
 * @author Peter Wu
 */
data class CachedValue(val value: String, val expiresIn: LocalDateTime) : Serializable {

    val expired: Boolean by lazy { expiresIn <= LocalDateTime.now() }
}