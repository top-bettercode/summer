package top.bettercode.summer.tools.lang.client

import ch.qos.logback.classic.Level
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.http.*
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import top.bettercode.summer.tools.lang.log.AlarmMarker
import top.bettercode.summer.tools.lang.operation.*
import top.bettercode.summer.tools.lang.operation.RequestConverter.extractHost
import top.bettercode.summer.tools.lang.util.StringUtil
import java.net.SocketTimeoutException
import java.time.LocalDateTime

/**
 *
 * @author Peter Wu
 */
class ClientHttpRequestLoggingInterceptor(
    private val collectionName: String,
    private val name: String,
    private val logMarker: String,
    private val logClazz: Class<*> = ClientHttpRequestLoggingInterceptor::class.java,
    private val timeoutAlarmSeconds: Int = -1,
    private val requestDecrypt: ((ByteArray) -> ByteArray)? = null,
    private val responseDecrypt: ((ByteArray) -> ByteArray)? = null
) : ClientHttpRequestInterceptor {

    private val log: Logger = LoggerFactory.getLogger(logClazz)

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val dateTime = if (log.isInfoEnabled) {
            LocalDateTime.now()
        } else null
        var response: ClientHttpResponse? = null
        var stackTrace = ""
        try {
            response = execution.execute(request, body)
            if (log.isInfoEnabled)
                response = ClientHttpResponseWrapper(response)
            return response!!
        } catch (e: Exception) {
            stackTrace = StringUtil.valueOf(e)
            throw e
        } finally {
            if (log.isInfoEnabled) {
                try {
                    var exception: Exception? = null
                    val operationResponse = if (response == null) OperationResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpHeaders(), ByteArray(0)
                    ) else try {
                        convert(response as ClientHttpResponseWrapper)
                    } catch (e: Exception) {
                        if (stackTrace.isBlank())
                            stackTrace = StringUtil.valueOf(e)
                        exception = e
                        OperationResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            HttpHeaders(), ByteArray(0)
                        )
                    }
                    operationResponse.stackTrace = stackTrace
                    val operation = Operation(
                        collectionName = collectionName,
                        name = name,
                        protocol = RequestConverter.DEFAULT_PROTOCOL,
                        request = convert(request, body, dateTime!!),
                        response = operationResponse
                    )
                    val isMultipart =
                        MediaType.MULTIPART_FORM_DATA.isCompatibleWith(request.headers.contentType)
                    val contentDisposition =
                        response?.headers?.getFirst(HttpHeaders.CONTENT_DISPOSITION)
                            ?.let { ContentDisposition.parse(it) }
                    val isFile = contentDisposition?.type != null && contentDisposition.type.equals(
                        "attachment",
                        ignoreCase = true
                    )

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
                } catch (e: Exception) {
                    log.error("日志记录异常", e)
                }
            }
        }
    }

    fun convert(request: HttpRequest, body: ByteArray, dateTime: LocalDateTime): OperationRequest {
        val headers = HttpHeaders()
        val cookies = request.headers[HttpHeaders.COOKIE]?.map {
            RequestCookie(it.substringBefore("="), it.substringAfter("="))
        } ?: listOf()

        val uri = request.uri

        headers["Host"] = extractHost(uri)

        headers.putAll(request.headers)
        headers.remove(HttpHeaders.COOKIE)

        val restUri = uri.toString()
        return OperationRequest(
            uri = uri,
            restUri = restUri,
            uriVariables = emptyMap(),
            method = request.method!!.name,
            headers = headers,
            cookies = cookies,
            remoteUser = "NonSpecificUser",
            queries = QueryStringParser.parse(uri),
            parts = listOf(),
            content = body,
            dateTime = dateTime
        )
    }

    fun convert(response: ClientHttpResponseWrapper): OperationResponse {
        val statusCode = try {
            response.statusCode.value()
        } catch (e: SocketTimeoutException) {
            0
        }
        val content = response.content
        return OperationResponse(
            statusCode,
            response.headers, content
        )
    }


}