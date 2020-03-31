package cn.bestwu.logging.websocket

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

@ServerEndpoint("/websocket/logging")
class WebSocketController {
    private val log = LoggerFactory.getLogger(WebSocketController::class.java)

    @OnOpen
    fun onOpen(session: Session) {
        sessions[session.id] = session
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
    @OnError
    fun onError(session: Session?, error: Throwable) {
        log.error(error.message, error)
    }

    /**
     * 服务器接收到客户端消息时调用的方法
     */
    @OnMessage
    fun onMessage(message: String?, session: Session?) {
    }

    companion object {
        private val sessions: MutableMap<String, Session> = ConcurrentHashMap()

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