package top.bettercode.logging.websocket

import org.slf4j.LoggerFactory
import top.bettercode.logging.WebsocketProperties
import top.bettercode.simpleframework.support.ApplicationContextHolder
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

@ServerEndpoint("/websocket/logging")
class WebSocketController() {

    private val log = LoggerFactory.getLogger(WebSocketController::class.java)


    @OnOpen
    fun onOpen(session: Session) {
        val token = session.requestParameterMap["token"]
        if (token.isNullOrEmpty() || websocketProperties.token != token.joinToString()) {
            log.warn("token:{}!={}", token?.joinToString(), websocketProperties.token)
            session.close(CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "非法连接"))
        } else {
            sessions[session.id] = session
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    fun onClose(session: Session) {
        sessions.remove(session.id)
    }

    /**
     * 发生错误时调用
     */
    @Suppress("UNUSED_PARAMETER")
    @OnError
    fun onError(session: Session?, error: Throwable) {
        log.error(error.message, error)
    }

    /**
     * 服务器接收到客户端消息时调用的方法
     */
    @Suppress("UNUSED_PARAMETER")
    @OnMessage
    fun onMessage(message: String?, session: Session?) {
    }

    companion object {
        private val sessions: MutableMap<String, Session> = ConcurrentHashMap()
        private val websocketProperties: WebsocketProperties by lazy {
            ApplicationContextHolder.getBean(WebsocketProperties::class.java)
        }

        @Throws(IOException::class)
        fun send(message: String?) {
            for (session in sessions.values) {
                if (session.isOpen) {
                    session.basicRemote.sendText(message)
                }
            }
        }
    }
}