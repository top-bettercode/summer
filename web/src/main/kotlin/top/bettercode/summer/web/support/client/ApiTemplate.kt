package top.bettercode.summer.web.support.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.lang.Nullable
import org.springframework.web.client.*
import org.springframework.web.util.UriTemplateHandler
import top.bettercode.summer.tools.lang.client.ClientHttpRequestWrapper
import java.lang.reflect.Type
import java.net.URI

/**
 * 请求模板
 *
 * @author Peter Wu
 */
open class ApiTemplate @JvmOverloads constructor(
        private val collectionName: String?, private val name: String?,
        protected val logMarker: String?,
        connectTimeout: Int,
        readTimeout: Int,
        private val requestDecrypt: ((ByteArray) -> ByteArray)? = null,
        private val responseDecrypt: ((ByteArray) -> ByteArray)? = null,
        protected val restTemplate: RestTemplate = object : RestTemplate() {
            private val log: Logger = LoggerFactory.getLogger(this.javaClass)
            override fun createRequest(url: URI, method: HttpMethod): ClientHttpRequest {
                return if (log.isInfoEnabled) {
                    ClientHttpRequestWrapper(collectionName = collectionName!!,
                            name = name!!,
                            logMarker = logMarker,
                            logClazz = javaClass,
                            request = super.createRequest(url, method),
                            requestDecrypt = requestDecrypt,
                            responseDecrypt = responseDecrypt)
                } else {
                    super.createRequest(url, method)
                }
            }
        }
) : RestOperations by restTemplate {
    protected val log: Logger = LoggerFactory.getLogger(this.javaClass)

    constructor(connectTimeout: Int, readTimeout: Int) : this("", "", connectTimeout, readTimeout)
    constructor(collectionName: String?, name: String?, connectTimeout: Int, readTimeout: Int) : this(collectionName, name, null, connectTimeout, readTimeout)

    init {
        System.setProperty("sun.net.client.defaultConnectTimeout", connectTimeout.toString())
        System.setProperty("sun.net.client.defaultReadTimeout", readTimeout.toString())

        val clientHttpRequestFactory = SimpleClientHttpRequestFactory()
        //Connect timeout
        clientHttpRequestFactory.setConnectTimeout(connectTimeout)
        //Read timeout
        clientHttpRequestFactory.setReadTimeout(readTimeout)
        clientHttpRequestFactory.setOutputStreaming(false)
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
