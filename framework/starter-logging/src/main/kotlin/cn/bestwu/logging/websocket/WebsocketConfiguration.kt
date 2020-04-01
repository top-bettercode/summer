package cn.bestwu.logging.websocket

import cn.bestwu.logging.websocket.WebSocketController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.*
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.support.ErrorPageFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.socket.server.standard.ServerEndpointExporter
import javax.websocket.server.ServerContainer

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

    @Bean
    fun serverEndpointExporter(): ServerEndpointExporter {
        return ServerEndpointExporter()
    }

    @Bean
    fun webSocketController(): WebSocketController {
        return WebSocketController()
    }

}
