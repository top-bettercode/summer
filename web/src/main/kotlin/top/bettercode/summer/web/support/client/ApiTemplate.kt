package top.bettercode.summer.web.support.client

import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.lang.Nullable
import org.springframework.web.client.*
import org.springframework.web.util.UriTemplateHandler
import top.bettercode.summer.tools.lang.log.OkHttpClientLoggingInterceptor
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * 请求模板
 *
 * @author Peter Wu
 */
open class ApiTemplate @JvmOverloads constructor(
        private val collectionName: String,
        private val name: String,
        protected val logMarker: String?,
        connectTimeout: Int,
        readTimeout: Int,
        private val requestDecrypt: ((ByteArray) -> ByteArray)? = null,
        private val responseDecrypt: ((ByteArray) -> ByteArray)? = null,
        protected val restTemplate: RestTemplate = RestTemplate()
) : RestOperations by restTemplate {
    protected val log: Logger = LoggerFactory.getLogger(this.javaClass)

    constructor(connectTimeout: Int, readTimeout: Int) : this("", "", connectTimeout, readTimeout)
    constructor(collectionName: String, name: String, connectTimeout: Int, readTimeout: Int) : this(collectionName, name, null, connectTimeout, readTimeout)

    init {
        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(OkHttpClientLoggingInterceptor(
                        collectionName = collectionName,
                        name = name,
                        logMarker = logMarker,
                        logClazz = this::class.java,
                        requestDecrypt = requestDecrypt,
                        responseDecrypt = responseDecrypt))
                .connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
                .readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)
                .build()

        val clientHttpRequestFactory = OkHttp3ClientHttpRequestFactory(okHttpClient)
        this.restTemplate.requestFactory = clientHttpRequestFactory
    }

    protected var errorHandler: ResponseErrorHandler
        get() = this.restTemplate.errorHandler
        set(errorHandler) {
            this.restTemplate.errorHandler = errorHandler
        }

    protected val uriTemplateHandler: UriTemplateHandler
        get() = this.restTemplate.uriTemplateHandler

    protected fun setMessageConverters(messageConverters: List<HttpMessageConverter<*>>) {
        this.restTemplate.messageConverters = messageConverters
    }

    protected fun setRequestFactory(requestFactory: ClientHttpRequestFactory) {
        this.restTemplate.requestFactory = requestFactory
    }

    protected fun <T> httpEntityCallback(@Nullable requestBody: Any?, responseType: Class<T>): RequestCallback {
        return this.restTemplate.httpEntityCallback<T>(requestBody, responseType)
    }

    protected fun <T> httpEntityCallback(@Nullable requestBody: Any?, responseType: Type): RequestCallback {
        return this.restTemplate.httpEntityCallback<T>(requestBody, responseType)
    }

    protected fun <T> responseEntityExtractor(responseType: Type): ResponseExtractor<ResponseEntity<T>> {
        return this.restTemplate.responseEntityExtractor(responseType)
    }

}
