package top.bettercode.summer.security.client

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.util.Assert

/**
 *
 * @author Peter Wu
 */
class ClientDetailsService(clientDetails: Collection<ClientDetails>) {

    val maxAccessTokenValiditySeconds: Int = clientDetails.maxOf { it.accessTokenValiditySeconds }
    val maxRefreshTokenValiditySeconds: Int = clientDetails.maxOf { it.refreshTokenValiditySeconds }

    val isSingleClient: Boolean = clientDetails.size == 1

    private val clientDetailsMap: Map<String, ClientDetails> =
            clientDetails.associateBy { it.clientId }

    fun getClientDetails(clientId: String): ClientDetails? {
        return clientDetailsMap[clientId]
    }

    fun authenticate(clientId: String, clientSecret: String) {
        val clientDetails = getClientDetails(clientId)
        val auth = clientDetails?.clientSecret == clientSecret
        if (!auth) {
            throw BadCredentialsException("Unauthorized")
        }
    }

    fun getClientId(): String {
        Assert.isTrue(clientDetailsMap.size == 1, "只能有一个客户端")
        return clientDetailsMap.keys.first()
    }
}