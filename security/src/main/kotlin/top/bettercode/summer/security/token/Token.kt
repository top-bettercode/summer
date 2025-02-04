package top.bettercode.summer.security.token

import java.time.Instant

class Token(val tokenValue: String, issuedAt: Instant?, expiresAt: Instant?) : InstantAt(issuedAt, expiresAt){
    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID = 1L
    }

}
