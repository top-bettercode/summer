package top.bettercode.summer.tools.lang.log

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import okio.GzipSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import top.bettercode.summer.tools.lang.operation.*
import top.bettercode.summer.tools.lang.util.StringUtil
import java.time.LocalDateTime

/**
 *
 * @author Peter Wu
 */
class OkHttpClientLoggingInterceptor(private val collectionName: String,
                                     private val name: String,
                                     private val logMarker: String? = null,
                                     private val requestDecrypt: ((ByteArray) -> ByteArray)? = null,
                                     private val responseDecrypt: ((ByteArray) -> ByteArray)? = null
) : Interceptor {
    private val log: Logger = LoggerFactory.getLogger(OkHttpClientLoggingInterceptor::class.java)


    override fun intercept(chain: Interceptor.Chain): Response {
        val dateTime =
                if (log.isInfoEnabled) {
                    LocalDateTime.now()
                } else null
        val request = chain.request()
        var response: Response? = null
        var stackTrace = ""
        try {
            response = chain.proceed(request)
            return response
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
                    convert(response)
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
                        protocol = chain.connection()?.protocol()?.toString()
                                ?: RequestConverter.DEFAULT_PROTOCOL,
                        request = convert(request, dateTime!!),
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

    fun convert(request: Request, dateTime: LocalDateTime): OperationRequest {
        val headers = HttpHeaders()
        for (header in request.headers) {
            headers.add(header.first, header.second)
        }
        val requestBody = request.body
        val content = if (requestBody != null) {
            requestBody.contentType()?.let {
                if (headers["Content-Type"] == null) {
                    headers["Content-Type"] = it.toString()
                }
            }
            if (requestBody.contentLength() != -1L) {
                if (headers["Content-Length"] == null) {
                    headers["Content-Length"] = requestBody.contentLength().toString()
                }
            }

            val buffer = Buffer()
            requestBody.writeTo(buffer)
            buffer.clone().readByteArray()
        } else {
            ByteArray(0)
        }
        val url = request.url
        if (headers.host == null) {
            headers["Host"] = "${url.host}:${url.port}"
        }
        val parameters = Parameters()
        for (parameterName in url.queryParameterNames) {
            parameters.add(parameterName, url.queryParameter(parameterName))
        }
        val uri = url.toUri()
        val restUri = uri.toString()
        val remoteUser = "NonSpecificUser"
        return OperationRequest(
                uri = uri,
                restUri = restUri,
                uriVariables = emptyMap(),
                method = request.method,
                headers = headers,
                cookies = emptyList(),
                remoteUser = remoteUser,
                parameters = parameters,
                parts = emptyList(),
                content = content,
                dateTime = dateTime
        )
    }

    fun convert(response: Response): OperationResponse {
        val headers = HttpHeaders()
        for (header in response.headers) {
            headers.add(header.first, header.second)
        }
        val responseBody = response.body
        val content = if (responseBody != null) {

            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            var buffer = source.buffer

            if ("gzip".equals(headers.getFirst("Content-Encoding"), true)) {
                GzipSource(buffer.clone()).use { gzippedResponseBody ->
                    buffer = Buffer()
                    buffer.writeAll(gzippedResponseBody)
                }
            }
            buffer.clone().readByteArray()
        } else {
            ByteArray(0)
        }
        return OperationResponse(
                response.code,
                headers, content
        )
    }
}