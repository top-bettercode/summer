package top.bettercode.summer.tools.lang.client

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import top.bettercode.summer.tools.lang.log.OkHttpClientLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * 请求模板
 *
 * @author Peter Wu
 */
open class ApiTemplate @JvmOverloads constructor(
        private val collectionName: String,
        private val name: String,
        protected val logMarker: String,
        timeoutAlarmSeconds: Int,
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
            timeoutAlarmSeconds = timeoutAlarmSeconds,
            requestDecrypt = requestDecrypt,
            responseDecrypt = responseDecrypt)

    @JvmField
    protected val okHttpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
            .readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)

    constructor(timeoutAlarmSeconds: Int, connectTimeout: Int, readTimeout: Int) : this(collectionName = "", name = "", timeoutAlarmSeconds = timeoutAlarmSeconds, connectTimeout = connectTimeout, readTimeout = readTimeout)

    constructor(collectionName: String, name: String, timeoutAlarmSeconds: Int, connectTimeout: Int, readTimeout: Int) : this(collectionName = collectionName, name = name, logMarker = "third-party", timeoutAlarmSeconds = timeoutAlarmSeconds, connectTimeout = connectTimeout, readTimeout = readTimeout)

    init {
        this.restTemplate.requestFactory = OkHttp3ClientHttpRequestFactory(okHttpClientBuilder.build())
    }

}
