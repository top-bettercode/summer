package top.bettercode.summer.tools.hikvision

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.client.SimpleClientHttpRequestFactory
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.hikvision.SignUtil.sign
import top.bettercode.summer.tools.hikvision.entity.EventData
import top.bettercode.summer.tools.hikvision.entity.EventRequest
import top.bettercode.summer.tools.hikvision.entity.HikvisionResponse
import top.bettercode.summer.tools.hikvision.entity.PageData
import top.bettercode.summer.tools.lang.client.ApiTemplate
import java.io.IOException
import java.net.HttpURLConnection
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 *
 * @author Peter Wu
 */
@LogMarker(HikvisionClient.MARKER)
class HikvisionClient(properties: HikvisionProperties) :
    ApiTemplate<HikvisionProperties>(MARKER, properties) {
    private val eventResponseType =
        object : ParameterizedTypeReference<HikvisionResponse<PageData<EventData>>>() {
        }


    init {
        this.requestFactory = object : SimpleClientHttpRequestFactory() {
            @Throws(IOException::class)
            override fun prepareConnection(connection: HttpURLConnection, httpMethod: String) {
                try {
                    val sslContext: SSLContext
                    val jdkVersion = System.getProperty("java.specification.version")
                    sslContext = if (jdkVersion.toDouble() >= SignUtil.JDK_VERSION) {
                        SSLContext.getInstance("TLSv1.2")
                    } else {
                        SSLContext.getInstance("TLS")
                    }
                    val tm: X509TrustManager = object : X509TrustManager {
                        override fun getAcceptedIssuers(): Array<X509Certificate>? {
                            return null
                        }

                        override fun checkClientTrusted(xcs: Array<X509Certificate>, str: String) {
                        }

                        override fun checkServerTrusted(xcs: Array<X509Certificate>, str: String) {
                        }
                    }
                    sslContext.init(null, arrayOf<TrustManager>(tm), null)
                    val httpsConnection = connection as HttpsURLConnection
                    httpsConnection.sslSocketFactory = sslContext.socketFactory
                    httpsConnection.hostnameVerifier =
                        HostnameVerifier { _: String?, _: SSLSession? -> true }
                } catch (ex: KeyManagementException) {
                    throw RuntimeException(ex)
                } catch (ex: NoSuchAlgorithmException) {
                    throw RuntimeException(ex)
                }
                super.prepareConnection(connection, httpMethod)
            }
        }
    }


    fun getEvents(request: EventRequest): PageData<EventData> {
        val properties = properties
        val method = HttpMethod.POST
        val path = properties.artemisPath + "/api/acs/v2/door/events"
        val headers = HttpHeaders()
        sign(headers, method.name, path, properties)

        val entity = exchange(
            properties.host + path, method,
            HttpEntity(request, headers),
            eventResponseType
        )
        val body = entity.body ?: throw clientException()
        return body.data ?: throw clientException()
    }


    companion object {
        const val MARKER: String = "hikvision"
    }
}
