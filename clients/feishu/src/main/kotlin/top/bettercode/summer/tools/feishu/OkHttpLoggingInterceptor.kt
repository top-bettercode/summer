package top.bettercode.summer.tools.feishu

import ch.qos.logback.classic.Level
import com.lark.oapi.okhttp.Interceptor
import com.lark.oapi.okhttp.Request
import com.lark.oapi.okhttp.Response
import com.lark.oapi.okio.Buffer
import com.lark.oapi.okio.GzipSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import top.bettercode.summer.tools.lang.log.AlarmMarker
import top.bettercode.summer.tools.lang.operation.*
import top.bettercode.summer.tools.lang.operation.RequestConverter.extractHost
import top.bettercode.summer.tools.lang.util.StringUtil
import java.time.LocalDateTime
import java.util.*

/**
 *
 * @author Peter Wu
 */
class OkHttpLoggingInterceptor(
    private val collectionName: String,
    private val name: String,
    private val logMarker: String,
    private val logClazz: Class<*> = OkHttpLoggingInterceptor::class.java,
    private val timeoutAlarmSeconds: Int = -1,
    private val requestDecrypt: ((ByteArray) -> ByteArray)? = null,
    private val responseDecrypt: ((ByteArray) -> ByteArray)? = null
) : Interceptor {

    private val clientLog: Logger = LoggerFactory.getLogger("CLIENT.$logMarker")
    private val log: Logger = LoggerFactory.getLogger(logClazz)

    override fun intercept(chain: Interceptor.Chain): Response {
        val dateTime =
            if (clientLog.isInfoEnabled) {
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
            if (clientLog.isInfoEnabled) {
                val operationResponse = if (response == null) OperationResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpHeaders.EMPTY, ByteArray(0)
                ) else {
                    convert(response)
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
                val isMultipart =
                    request.headers()[HttpHeaders.CONTENT_TYPE]?.lowercase(Locale.getDefault())
                        ?.startsWith("multipart/") == true
                val isFile =
                    !response?.headers()?.get(HttpHeaders.CONTENT_DISPOSITION).isNullOrBlank()

                var msg = operation.toString(
                    RequestLoggingConfig(
                        includeRequestBody = !isMultipart,
                        includeResponseBody = !isFile,
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

                val marker = MarkerFactory.getDetachedMarker(logMarker)
                if (timeoutAlarmSeconds > 0 && operation.duration > timeoutAlarmSeconds * 1000) {
                    val initialComment =
                        "${operation.name}(${operation.request.restUri})：请求响应速度慢"
                    val timeoutMsg = "(${operation.duration / 1000}秒)"
                    marker.add(
                        AlarmMarker(
                            message = initialComment + timeoutMsg,
                            timeout = true,
                            level = Level.WARN
                        )
                    )
                    msg = "$initialComment${timeoutMsg}\n$msg"
                    clientLog.warn(marker, msg)
                } else {
                    val hasException = stackTrace.isNotBlank()
                    if (hasException || operation.duration > 2 * 1000) {
                        clientLog.warn(marker, msg)
                    } else
                        clientLog.info(marker, msg)
                }
            }
        }
    }

    fun convert(request: Request, dateTime: LocalDateTime): OperationRequest {
        val headers = HttpHeaders()
        for (name in request.headers().names()) {
            headers.add(name, request.headers()[name])
        }
        val requestBody = request.body()
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
        val url = request.url()
        val uri = url.uri()
        headers["Host"] = extractHost(uri)

        val queries = Parameters()
        for (parameterName in url.queryParameterNames()) {
            queries.add(parameterName, url.queryParameter(parameterName))
        }
        val restUri = uri.toString()
        val remoteUser = "NonSpecificUser"
        return OperationRequest(
            uri = uri,
            restUri = restUri,
            method = request.method(),
            headers = headers,
            remoteUser = remoteUser,
            queries = queries,
            content = content,
            dateTime = dateTime
        )
    }

    fun convert(response: Response): OperationResponse {
        val statusCode = try {
            response.code()
        } catch (e: Exception) {
            log.warn("Failed to get response code", e)
            0
        }

        val headers = try {
            val headers = HttpHeaders()
            val respHeaders = response.headers()
            for (name in respHeaders.names()) {
                headers.add(name, respHeaders[name])
            }
            headers
        } catch (e: Exception) {
            log.warn("Failed to get response headers", e)
            HttpHeaders()
        }

        val responseBody = try {
            response.body()
        } catch (e: Exception) {
            log.warn("Failed to get response body", e)
            null
        }
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
        return OperationResponse(statusCode, headers, content)
    }
}