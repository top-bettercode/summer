package top.bettercode.summer.tools.lang.client

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory
import org.springframework.lang.Nullable
import org.springframework.web.client.*
import top.bettercode.summer.tools.lang.log.OkHttpClientLoggingInterceptor
import java.io.IOException
import java.lang.reflect.Type
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * 请求模板
 *
 * @author Peter Wu
 */
open class ApiTemplate<P : ClientProperties> @JvmOverloads constructor(
    protected val logMarker: String,
    @JvmField
    val properties: P,
    private val requestDecrypt: ((ByteArray) -> ByteArray)? = null,
    private val responseDecrypt: ((ByteArray) -> ByteArray)? = null,
    private val collectionName: String = "第三方平台",
) : RestTemplate() {

    protected val log: Logger = LoggerFactory.getLogger(this.javaClass)

    private val loggingInterceptor: Interceptor = OkHttpClientLoggingInterceptor(
        collectionName = collectionName,
        name = properties.platformName,
        logMarker = logMarker,
        logClazz = this::class.java,
        timeoutAlarmSeconds = properties.timeoutAlarmSeconds,
        requestDecrypt = requestDecrypt,
        responseDecrypt = responseDecrypt
    )

    @JvmField
    protected val okHttpClientBuilder = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(properties.connectTimeout.toLong(), TimeUnit.SECONDS)
        .readTimeout(properties.readTimeout.toLong(), TimeUnit.SECONDS)


    init {
        this.requestFactory =
            OkHttp3ClientHttpRequestFactory(okHttpClientBuilder.build())
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
            else
                throw clientException(e)
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
                    (ResponseEntity.status(response.rawStatusCode)
                        .headers(response.headers) as ResponseEntity.BodyBuilder).body(body)
                } else {
                    (ResponseEntity.status(response.rawStatusCode)
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

    @JvmOverloads
    fun clientException(
        cause: Throwable?,
        response: Any? = null
    ): ClientException {
        return ClientException(
            platformName = properties.platformName,
            marker = logMarker,
            originalMessage = if (cause is ClientException) cause.originalMessage else cause?.message,
            cause = cause,
            response = response
        )

    }

    @JvmOverloads
    fun clientException(
        message: String? = null,
        response: Any? = null
    ): ClientException {
        return ClientException(
            platformName = properties.platformName,
            marker = logMarker,
            originalMessage = message,
            cause = null,
            response = response
        )

    }

    @JvmOverloads
    fun clientSysException(
        cause: Throwable?,
        response: Any? = null
    ): ClientSysException {
        return ClientSysException(
            platformName = properties.platformName,
            marker = logMarker,
            originalMessage = if (cause is ClientException) cause.originalMessage else cause?.message,
            cause = cause,
            response = response
        )
    }

    @JvmOverloads
    fun clientSysException(
        message: String? = null,
        response: Any? = null
    ): ClientSysException {
        return ClientSysException(
            platformName = properties.platformName,
            marker = logMarker,
            originalMessage = message,
            cause = null,
            response = response
        )
    }
}
