package top.bettercode.summer.tools.lang.client

import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.ClientHttpResponse
import top.bettercode.summer.tools.lang.operation.*
import top.bettercode.summer.tools.lang.util.StringUtil
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
    private val logMarker: String?,
    private val request: ClientHttpRequest,
    private val requestDecrypt: ((ByteArray) -> ByteArray)? = null,
    private val responseDecrypt: ((ByteArray) -> ByteArray)? = null
) : ClientHttpRequest {
    private val log = LoggerFactory.getLogger(ClientHttpRequestWrapper::class.java)
    val record = ByteArrayOutputStream()

    override fun execute(): ClientHttpResponse {
        val dateTime =
            if (log.isInfoEnabled) {
                LocalDateTime.now()
            } else null
        var response: ClientHttpResponse? = null
        var stackTrace = ""
        try {
            response = request.execute()
            if (log.isInfoEnabled)
                response = ClientHttpResponseWrapper(response)
            return response!!
        } catch (e: Exception) {
            stackTrace = StringUtil.valueOf(e)
            throw e
        } finally {
            if (log.isInfoEnabled) {
                var exception: Exception? = null
                val operationResponse = if (response == null) OperationResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpHeaders.EMPTY, ByteArray(0)
                ) else try {
                    ResponseConverter.convert(response as ClientHttpResponseWrapper)
                } catch (e: Exception) {
                    if (stackTrace.isBlank())
                        stackTrace = StringUtil.valueOf(e)
                    exception = e
                    OperationResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpHeaders.EMPTY, ByteArray(0)
                    )
                }
                operationResponse.stackTrace = stackTrace
                val operation = Operation(
                    collectionName = collectionName,
                    name = name,
                    protocol = "HTTP/1.1",
                    request = RequestConverter.convert(this, dateTime!!),
                    response = operationResponse
                )
                val msg = operation.toString(
                    RequestLoggingConfig(
                        includeRequestBody = true,
                        includeResponseBody = true,
                        includeTrace = true,
                        encryptHeaders = arrayOf(),
                        encryptParameters = arrayOf(),
                        format = true,
                        ignoredTimeout = true,
                        timeoutAlarmSeconds = -1
                    ),
                    requestDecrypt,
                    responseDecrypt
                )
                val hasException = stackTrace.isNotBlank()
                if (logMarker.isNullOrBlank()) {
                    if (hasException) {
                        log.warn(msg)
                    } else
                        log.info(msg)
                } else {
                    if (hasException) {
                        log.warn(MarkerFactory.getMarker(logMarker), msg)
                    } else
                        log.info(MarkerFactory.getMarker(logMarker), msg)
                }
                if (exception != null) {
                    throw exception
                }
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

    override fun getBody(): OutputStream {
        return OutputStreamWrapper(request.body)
    }

    private inner class OutputStreamWrapper(private val delegate: OutputStream) :
        OutputStream() {
            override fun write(b: Int) {
            delegate.write(b)
            record.write(b)
        }
    }
}