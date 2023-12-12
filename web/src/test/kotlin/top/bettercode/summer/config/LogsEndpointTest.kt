package top.bettercode.summer.config

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.mock.env.MockEnvironment
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
        val response = MockHttpServletResponse()
        val logsEndpoint = LogsEndpoint(
                "/local/downloads",
                MockEnvironment(),
                WebsocketProperties(),
                response,
                WebEndpointProperties(),
                ManagementServerProperties() )
        logsEndpoint.path("log_total.log", false, null)
        println(String(GZIPInputStream(ByteArrayInputStream(response.contentAsByteArray)).readBytes()))
    }
}