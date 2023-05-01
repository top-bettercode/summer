package top.bettercode.summer.logging.websocket

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.server.standard.ServerEndpointExporter
import top.bettercode.summer.logging.WebsocketProperties

/**
 * 自动增加请求日志过滤器
 *
 * @author Peter Wu
 * @since 0.1.5
 */
@ConditionalOnWebApplication
@ConditionalOnClass(org.springframework.web.socket.server.standard.ServerEndpointExporter::class)
@ConditionalOnProperty(
    prefix = "summer.logging.websocket",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
@Configuration(proxyBeanMethods = false)
class WebsocketConfiguration {

    @Bean
    fun serverEndpointExporter(): ServerEndpointExporter {
        return ServerEndpointExporter()
    }

    @Bean
    fun webSocketController(websocketProperties: WebsocketProperties): WebSocketController {
        return WebSocketController(websocketProperties)
    }

}
