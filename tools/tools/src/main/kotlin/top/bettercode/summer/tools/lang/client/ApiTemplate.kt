package top.bettercode.summer.tools.lang.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.InterceptingClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.lang.Nullable
import org.springframework.web.client.*
import java.io.IOException
import java.lang.reflect.Type
import java.net.SocketTimeoutException
import java.net.URI
import java.net.UnknownHostException

/**
 * 请求模板
 *
 * @author Peter Wu
 */
open class ApiTemplate<P : ClientProperties> @JvmOverloads constructor(
    final override val marker: String,
    val properties: P,
    private val requestDecrypt: ((ByteArray) -> ByteArray)? = null,
    private val responseDecrypt: ((ByteArray) -> ByteArray)? = null,
    private val collectionName: String = "第三方平台",
    override val platformName: String = properties.platformName,
    requestFactory: ClientHttpRequestFactory = SimpleClientHttpRequestFactory().apply {
        setConnectTimeout(properties.connectTimeout * 1000)
        setReadTimeout(properties.readTimeout * 1000)
    }
) : RestTemplate(), ApiExceptions {

    protected val log: Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        val loggingInterceptor = ClientHttpRequestLoggingInterceptor(
            collectionName = collectionName,
            name = properties.platformName,
            logMarker = marker,
            logClazz = this::class.java,
            timeoutAlarmSeconds = properties.timeoutAlarmSeconds,
            requestDecrypt = requestDecrypt,
            responseDecrypt = responseDecrypt
        )

        this.requestFactory = InterceptingClientHttpRequestFactory(
            requestFactory, listOf(loggingInterceptor)
        )
    }

    //--------------------------------------------

    override fun <T : Any?> doExecute(
        url: URI,
        method: HttpMethod?,
        requestCallback: RequestCallback?,
        responseExtractor: ResponseExtractor<T>?
    ): T? {
        try {
            return super.doExecute(url, method, requestCallback, responseExtractor)
        } catch (e: Exception) {
            if (e is ClientException)
                throw e
            else {
                if (e.cause is SocketTimeoutException || e.cause is UnknownHostException) {
                    throw clientException(message = "连接超时", cause = e)
                } else
                    throw clientException(e)
            }
        }
    }

    //--------------------------------------------

    override fun <T : Any?> responseEntityExtractor(responseType: Type): ResponseExtractor<ResponseEntity<T>> {
        return ResponseEntityResponseExtractor(responseType)
    }

    private inner class ResponseEntityResponseExtractor<T>(@Nullable responseType: Type?) :
        ResponseExtractor<ResponseEntity<T>> {
        @Nullable
        private var delegate: HttpMessageConverterExtractor<T>? = null

        init {
            if (responseType != null && Void::class.java != responseType) {
                this.delegate = HttpMessageConverterExtractor(
                    responseType,
                    this@ApiTemplate.messageConverters
                )
            } else {
                this.delegate = null
            }
        }

        @Throws(IOException::class)
        override fun extractData(response: ClientHttpResponse): ResponseEntity<T> {
            val responseEntity = try {
                if (this.delegate != null) {
                    val body = delegate!!.extractData(response)
                    (ResponseEntity.status(response.statusCode)
                        .headers(response.headers) as ResponseEntity.BodyBuilder).body(body)
                } else {
                    (ResponseEntity.status(response.statusCode)
                        .headers(response.headers) as ResponseEntity.BodyBuilder).build()
                }
            } catch (e: Exception) {
                throw clientException(e)
            } ?: throw clientException()

            val body = responseEntity.body
            return if (responseEntity.statusCode.is2xxSuccessful) {
                if (body is ClientResponse) {
                    if (body.isOk) {
                        responseEntity
                    } else {
                        throw clientSysException(message = body.message, response = body)
                    }
                } else
                    responseEntity
            } else {
                throw clientSysException(
                    message = if (body is ClientResponse) body.message else null,
                    response = body
                )
            }
        }
    }

    //--------------------------------------------

}
