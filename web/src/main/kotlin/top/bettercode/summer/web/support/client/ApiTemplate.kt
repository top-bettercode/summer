package top.bettercode.summer.web.support.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import top.bettercode.summer.tools.lang.client.ClientHttpRequestWrapper
import java.net.URI

/**
 * zabbix请求模板
 *
 * @author Peter Wu
 */
open class ApiTemplate @JvmOverloads constructor(
        private val collectionName: String?, private val name: String?,
        protected val logMarker: String?,
        connectTimeout: Int,
        readTimeout: Int,
        private val requestDecrypt: ((ByteArray) -> ByteArray)? = null,
        private val responseDecrypt: ((ByteArray) -> ByteArray)? = null
) : RestTemplate() {
    protected val log: Logger = LoggerFactory.getLogger(this.javaClass)

    constructor(connectTimeout: Int, readTimeout: Int) : this("", "", connectTimeout, readTimeout)
    constructor(collectionName: String?, name: String?, connectTimeout: Int, readTimeout: Int) : this(collectionName, name, null, connectTimeout, readTimeout)

    init {
        val clientHttpRequestFactory = SimpleClientHttpRequestFactory()
        //Connect timeout
        clientHttpRequestFactory.setConnectTimeout(connectTimeout)
        //Read timeout
        clientHttpRequestFactory.setReadTimeout(readTimeout)
        clientHttpRequestFactory.setOutputStreaming(false)
        requestFactory = clientHttpRequestFactory
    }

    override fun createRequest(url: URI, method: HttpMethod): ClientHttpRequest {
        return if (log.isInfoEnabled) {
            ClientHttpRequestWrapper(collectionName!!, name!!, logMarker,
                    super.createRequest(url, method), requestDecrypt, responseDecrypt)
        } else {
            super.createRequest(url, method)
        }
    }
}
