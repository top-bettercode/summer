package top.bettercode.summer.tools.lang.client

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory
import org.springframework.lang.Nullable
import org.springframework.web.client.RequestCallback
import org.springframework.web.client.ResponseExtractor
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
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
        @JvmField
        protected val restTemplate: RestTemplate = RestTemplate()
) : RestOperations by restTemplate {
    protected val log: Logger = LoggerFactory.getLogger(this.javaClass)
    private val loggingInterceptor: Interceptor = OkHttpClientLoggingInterceptor(
            collectionName = collectionName,
            name = name,
            logMarker = logMarker,
            logClazz = this::class.java,
            requestDecrypt = requestDecrypt,
            responseDecrypt = responseDecrypt)
    @JvmField
    protected val okHttpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
            .readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)

    constructor(connectTimeout: Int, readTimeout: Int) : this("", "", connectTimeout, readTimeout)
    constructor(collectionName: String, name: String, connectTimeout: Int, readTimeout: Int) : this(collectionName, name, null, connectTimeout, readTimeout)

    init {
        this.restTemplate.requestFactory = OkHttp3ClientHttpRequestFactory(okHttpClientBuilder.build())
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
