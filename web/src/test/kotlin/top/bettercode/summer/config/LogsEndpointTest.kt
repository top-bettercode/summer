package top.bettercode.summer.config

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties
import org.springframework.mock.env.MockEnvironment
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import top.bettercode.summer.logging.WebsocketProperties
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

/**
 *
 * @author Peter Wu
 */
class LogsEndpointTest {

    @Disabled
    @Test
    fun path() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val logsEndpoint = LogsEndpoint(
            "/local/downloads",
            MockEnvironment(),
            WebsocketProperties(),
            request,
            response,
            WebEndpointProperties(),
            ManagementServerProperties()
        )
        logsEndpoint.path(
            path = "log_total.log",
            collapse = false,
            download = null,
            traceid = null,
            userAgent = null
        )
        println(String(GZIPInputStream(ByteArrayInputStream(response.contentAsByteArray)).readBytes()))
    }
}