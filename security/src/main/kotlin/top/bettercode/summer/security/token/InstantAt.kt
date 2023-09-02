package top.bettercode.summer.security.token

import java.io.Serializable
import java.time.Instant

open class InstantAt(val issuedAt: Instant?, val expiresAt: Instant?) : Serializable {

    val isExpired: Boolean
        //--------------------------------------------
        get() = expiresAt != null && Instant.now().isAfter(expiresAt)

    @Suppress("PropertyName")
    val expires_in: Int
        get() = if (expiresAt != null) java.lang.Long.valueOf(
                (expiresAt.toEpochMilli() - System.currentTimeMillis()) / 1000L).toInt() else -1

    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID = 1L
    }
}
