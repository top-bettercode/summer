package cn.bestwu.logging.client

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.StreamUtils
import java.io.IOException
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * 响应结果包装类 可反复取响应内容
 *
 * @author Peter Wu
 */
class ClientHttpResponseWrapper(private val response: ClientHttpResponse?) : ClientHttpResponse {

    val bytes: ByteArray =
        if (response != null) StreamUtils.copyToByteArray(response.body) else ByteArray(0)

    override fun getHeaders(): HttpHeaders {
        return response?.headers ?: HttpHeaders.EMPTY
    }

    @Throws(IOException::class)
    override fun getBody(): InputStream {
        return ByteArrayInputStream(bytes)
    }

    @Throws(IOException::class)
    override fun getStatusCode(): HttpStatus {
        return response?.statusCode ?: HttpStatus.INTERNAL_SERVER_ERROR
    }

    @Throws(IOException::class)
    override fun getRawStatusCode(): Int {
        return response?.rawStatusCode ?: HttpStatus.INTERNAL_SERVER_ERROR.value()
    }

    @Throws(IOException::class)
    override fun getStatusText(): String {
        return response?.statusText ?: ""
    }

    override fun close() {
        response?.close()
    }

}