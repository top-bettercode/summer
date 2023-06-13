package top.bettercode.summer.tools.lang.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.StreamUtils
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

/**
 * 响应结果包装类 可反复取响应内容
 *
 * @author Peter Wu
 */
class ClientHttpResponseWrapper(private val response: ClientHttpResponse) : ClientHttpResponse {

    private val log: Logger = LoggerFactory.getLogger(ClientHttpResponseWrapper::class.java)

    var bytes: ByteArray? = null
    val content: ByteArray
        get() {
            if (bytes == null) {
                bytes = try {
                    StreamUtils.copyToByteArray(response.body)
                } catch (e: Exception) {
                    log.warn("读取响应错误", e)
                    ByteArray(0)
                }
            }
            return bytes!!
        }

    override fun getHeaders(): HttpHeaders {
        return response.headers
    }

    override fun getBody(): InputStream {
        return ByteArrayInputStream(content)
    }


    override fun getStatusCode(): HttpStatus {
        return response.statusCode
    }

    override fun getRawStatusCode(): Int {
        return response.rawStatusCode
    }

    override fun getStatusText(): String {
        return response.statusText
    }

    override fun close() {
        response.close()
    }

}