package top.bettercode.summer.logging.feign

import feign.Logger
import feign.Request
import feign.Response
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.http.HttpHeaders
import top.bettercode.summer.tools.lang.operation.*
import top.bettercode.summer.tools.lang.operation.HttpOperation.SEPARATOR_LINE
import top.bettercode.summer.tools.lang.operation.RequestConverter.extractHost
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.lang.util.TimeUtil
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN
import java.io.IOException
import java.net.URI
import java.time.LocalDateTime

/**
 *
 * @author Peter Wu
 */
class FeignLogger : Logger() {

    private val log = LoggerFactory.getLogger(FeignLogger::class.java)
    private val logMarker = "feign"
    private val marginLine = "============================================================"

    override fun logRequest(configKey: String, logLevel: Level, request: Request) {
        if (Level.FULL == logLevel) {
            val headers = HttpHeaders()
            request.headers().forEach { (name, values) ->
                headers.addAll(name, values.toList())
            }
            val requestBody = request.body() ?: ByteArray(0)
            val uri = URI(request.url())
            headers["Host"] = extractHost(uri)

            val parameters = Parameters()

            val restUri = uri.rawPath
            val remoteUser = "NonSpecificUser"
            val operationRequest = OperationRequest(
                uri = uri,
                restUri = restUri,
                uriVariables = emptyMap(),
                method = request.httpMethod().name,
                headers = headers,
                cookies = emptyList(),
                remoteUser = remoteUser,
                parameters = parameters,
                parts = emptyList(),
                content = requestBody,
                dateTime = LocalDateTime.now()
            )
            val stringBuilder = StringBuilder("")
            stringBuilder.appendLine()
            stringBuilder.appendLine(marginLine)
            stringBuilder.appendLine("feign/$configKey")
            stringBuilder.appendLine(
                "REQUEST    TIME : ${
                    TimeUtil.format(
                        operationRequest.dateTime,
                        DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN
                    )
                }"
            )
            stringBuilder.appendLine(SEPARATOR_LINE)
            stringBuilder.append(
                HttpOperation.toString(
                    operationRequest,
                    RequestConverter.DEFAULT_PROTOCOL,
                    true
                )
            )
            stringBuilder.appendLine(SEPARATOR_LINE)

            log.info(MarkerFactory.getMarker(logMarker), stringBuilder.toString())
        } else {
            log.info(MarkerFactory.getMarker(logMarker), "feign/$configKey:${request.url()}")
        }
    }

    override fun logAndRebufferResponse(
        configKey: String,
        logLevel: Level,
        response: Response,
        elapsedTime: Long
    ): Response {
        if (Level.FULL == logLevel) {
            val headers = HttpHeaders()
            response.headers().forEach { (name, values) ->
                headers.addAll(name, values.toList())
            }

            val responseBody = response.body().asInputStream()?.readBytes()

            val operationResponse = OperationResponse(
                response.status(),
                headers, responseBody ?: ByteArray(0)
            )

            val stringBuilder = StringBuilder("")
            stringBuilder.appendLine()
            stringBuilder.appendLine(SEPARATOR_LINE)
            stringBuilder.appendLine("feign/$configKey")
            stringBuilder.appendLine(
                "RESPONSE   TIME : ${
                    TimeUtil.format(
                        operationResponse.dateTime,
                        DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN
                    )
                }"
            )
            stringBuilder.appendLine("DURATION MILLIS : $elapsedTime")
            stringBuilder.appendLine(SEPARATOR_LINE)
            stringBuilder.appendLine()
            stringBuilder.append(
                HttpOperation.toString(
                    operationResponse,
                    RequestConverter.DEFAULT_PROTOCOL,
                    true
                )
            )
            stringBuilder.appendLine(marginLine)
            log.info(MarkerFactory.getMarker(logMarker), stringBuilder.toString())
            return response.toBuilder().body(responseBody).build()
        } else {
            return response
        }
    }

    override fun logIOException(
        configKey: String,
        logLevel: Level,
        ioe: IOException,
        elapsedTime: Long
    ): IOException {
        if (Level.FULL == logLevel) {
            val stringBuilder = StringBuilder("")
            stringBuilder.appendLine()
            stringBuilder.appendLine(SEPARATOR_LINE)
            stringBuilder.appendLine("feign/$configKey")
            val stackTrace = StringUtil.valueOf(ioe)
            stringBuilder.appendLine(SEPARATOR_LINE)
            stringBuilder.appendLine("StackTrace:")
            stringBuilder.appendLine(stackTrace)
            stringBuilder.appendLine(marginLine)

            log.info(MarkerFactory.getMarker(logMarker), stringBuilder.toString())
        }
        return ioe
    }

    override fun log(configKey: String?, format: String?, vararg args: Any?) {
    }
}