package top.bettercode.summer.config

import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.core.env.Environment
import org.springframework.mock.env.MockEnvironment
import org.springframework.mock.web.MockHttpServletResponse
import top.bettercode.summer.logging.WebsocketProperties
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 *
 * @author Peter Wu
 */
class LogsEndpointTest {

    @Test
    fun path() {
        val response = MockHttpServletResponse()
        val logsEndpoint = LogsEndpoint(
                "/local/downloads",
                MockEnvironment(),
                WebsocketProperties(),
                ServerProperties(),
                response,
                WebEndpointProperties()
        )
        logsEndpoint.path("log_total.log",false,null)
        println(String(GZIPInputStream(ByteArrayInputStream(response.contentAsByteArray)).readBytes()))
    }
}