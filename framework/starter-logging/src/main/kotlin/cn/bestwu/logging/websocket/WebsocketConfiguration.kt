package cn.bestwu.logging.websocket

import cn.bestwu.logging.RequestLoggingProperties
import cn.bestwu.logging.WebsocketProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.*
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.server.standard.ServerEndpointExporter

/**
 * 自动增加请求日志过滤器
 *
 * @author Peter Wu
 * @since 0.1.5
 */
@ConditionalOnWebApplication
@ConditionalOnClass(org.springframework.web.socket.server.standard.ServerEndpointExporter::class)
@ConditionalOnProperty(prefix = "logging.websocket", name = ["enabled"], havingValue = "true", matchIfMissing = false)
@Configuration
class WebsocketConfiguration {

    private val log: Logger = LoggerFactory.getLogger(WebsocketConfiguration::class.java)

    @Bean
    fun serverEndpointExporter(applicationContext: ApplicationContext): ServerEndpointExporter {
        WebSocketController.applicationContext = applicationContext
        return ServerEndpointExporter()
    }

    @Bean
    fun webSocketController(websocketProperties: WebsocketProperties): WebSocketController {
        return WebSocketController()
    }

}