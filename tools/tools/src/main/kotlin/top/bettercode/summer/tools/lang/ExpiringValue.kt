package top.bettercode.summer.tools.lang

import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime

/**
 *
 * @author Peter Wu
 */
data class ExpiringValue<T>(
    val originalValue: T,
    val expiresIn: Duration,
    val expiresTime: LocalDateTime = LocalDateTime.now().plus(expiresIn)
) : Serializable {

    val value: T?
        get() {
            return if (expired()) null else originalValue
        }

    fun expired(): Boolean {
        return !LocalDateTime.now().isBefore(expiresTime)
    }

    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID = 1L
    }
}