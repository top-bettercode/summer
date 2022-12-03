package top.bettercode.summer.test.autodoc

import org.hibernate.validator.constraints.Length
import java.util.*
import javax.validation.constraints.NotNull

/**
 * 客户端TOKEN 对应表名：OAUTH_CLIENT_TOKEN
 */
data class ClientToken(

        /**
         * TOKEN ID
         */
        @Length(max = 256)
        var tokenId: String? = null,

        /**
         * TOKEN
         */
        var token: ByteArray? = null,

        /**
         * 权限信息ID
         */
        var authenticationId: String? = null,

        /**
         * 用户名
         */
        @field:NotNull
        @Length(max = 256)
        var userName: String? = null,

        /**
         * 客户端ID
         */
        @field:NotNull(groups = [Create::class])
        var clientId: Int? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClientToken) return false

        if (tokenId != other.tokenId) return false
        if (!Arrays.equals(token, other.token)) return false
        if (authenticationId != other.authenticationId) return false
        if (userName != other.userName) return false
        if (clientId != other.clientId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tokenId?.hashCode() ?: 0
        result = 31 * result + (token?.contentHashCode() ?: 0)
        result = 31 * result + (authenticationId?.hashCode() ?: 0)
        result = 31 * result + (userName?.hashCode() ?: 0)
        result = 31 * result + (clientId ?: 0)
        return result
    }
}
