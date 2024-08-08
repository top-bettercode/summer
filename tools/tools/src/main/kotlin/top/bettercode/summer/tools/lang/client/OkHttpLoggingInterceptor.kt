package top.bettercode.summer.tools.lang.client

import ch.qos.logback.classic.Level
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
    private val log: Logger = LoggerFactory.getLogger(logClazz)


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
                val isMultipart =
                    request.headers[HttpHeaders.CONTENT_TYPE]?.lowercase(Locale.getDefault())
                        ?.startsWith("multipart/") == true
                val isFile =
                    !response?.headers?.get(HttpHeaders.CONTENT_DISPOSITION).isNullOrBlank()

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
                    log.warn(marker, msg)
                } else {
                    val hasException = stackTrace.isNotBlank()
                    if (hasException || operation.duration > 2 * 1000) {
                        log.warn(marker, msg)
                    } else
                        log.info(marker, msg)
                }
                if (exception != null) {
                    throw exception
                }
            }
        }
    }

    fun convert(request: Request, dateTime: LocalDateTime): OperationRequest {
        val headers = HttpHeaders()
        for (name in request.headers.names()) {
            headers.add(name, request.headers[name])
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
        headers["Host"] = extractHost(url.toUri())

        val queries = Parameters()
        for (parameterName in url.queryParameterNames) {
            queries.add(parameterName, url.queryParameter(parameterName))
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
            queries = queries,
            parts = emptyList(),
            content = content,
            dateTime = dateTime
        )
    }

    fun convert(response: Response): OperationResponse {
        val headers = HttpHeaders()
        for (name in response.headers.names()) {
            headers.add(name, response.headers[name])
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