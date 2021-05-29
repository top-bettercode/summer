package cn.bestwu.logging.client

import cn.bestwu.logging.RequestLoggingConfig
import cn.bestwu.logging.operation.Operation
import cn.bestwu.logging.operation.OperationResponse
import cn.bestwu.logging.operation.RequestConverter
import cn.bestwu.logging.operation.ResponseConverter
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.ClientHttpResponse
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URI
import java.time.LocalDateTime

/**
 * @author Peter Wu
 */
class ClientHttpRequestWrapper(
    private val collectionName: String,
    private val name: String,
    private val request: ClientHttpRequest
) : ClientHttpRequest {
    private val log = LoggerFactory.getLogger(ClientHttpRequestWrapper::class.java)
    val record = ByteArrayOutputStream()

    @Throws(IOException::class)
    override fun execute(): ClientHttpResponse {
        val dateTime =
            if (log.isInfoEnabled) {
                LocalDateTime.now()
            } else null
        var response: ClientHttpResponse? = null
        try {
            response = request.execute()
            if (log.isInfoEnabled)
                response = ClientHttpResponseWrapper(response)
            return response!!
        } finally {
            if (log.isInfoEnabled) {
                val operation = Operation(
                    collectionName = collectionName,
                    name = name,
                    protocol = "HTTP/1.1",
                    request = RequestConverter.convert(this, dateTime!!),
                    response = if (response == null) OperationResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpHeaders.EMPTY, ByteArray(0)
                    ) else ResponseConverter.convert(response as ClientHttpResponseWrapper)
                )
                log.info(
                    operation.toString(
                        RequestLoggingConfig(
                            includeRequestBody = true,
                            includeResponseBody = true,
                            includeTrace = true,
                            encryptHeaders = arrayOf(),
                            encryptParameters = arrayOf(),
                            format = true,
                            ignoredTimeout = true,
                            timeoutAlarmSeconds = -1
                        )
                    )
                )
            }
        }
    }

    override fun getMethod(): HttpMethod {
        return request.method!!
    }

    override fun getMethodValue(): String {
        return request.methodValue
    }

    override fun getURI(): URI {
        return request.uri
    }

    override fun getHeaders(): HttpHeaders {
        return request.headers
    }

    @Throws(IOException::class)
    override fun getBody(): OutputStream {
        return OutputStreamWrapper(request.body)
    }

    private inner class OutputStreamWrapper constructor(private val delegate: OutputStream) :
        OutputStream() {
        @Throws(IOException::class)
        override fun write(b: Int) {
            delegate.write(b)
            record.write(b)
        }
    }
}