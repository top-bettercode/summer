package top.bettercode.summer.logging.socket

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

object SocketTestServer {

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            var server: ServerSocket? = null
            try {
                server = ServerSocket(4560)
            } catch (e: Exception) {
                println("can not listen to:$e")
            }

            while (true) {
                try {
                    val socket: Socket = server!!.accept()
                    val `is` = BufferedReader(InputStreamReader(socket.getInputStream()))
                    val os = PrintWriter(socket.getOutputStream())
                    `is`.lines().forEach {
                        println("Client:$it")
                    }
                    os.println(
                        "HTTP/1.1 200 OK\n"
                                + "Date: Sat, 31 Dec 2005 23:59:59 GMT\n"
                                + "Content-Type: text/html;charset=ISO-8859-1\n"
                                + "\n"
                                + "result"
                    )
                    os.print("finish")
                    System.err.println("finish")
                    os.flush()
                    os.close() //关闭Socket输出流
                    `is`.close() //关闭Socket输入流
                    socket.close() //关闭Socket
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        } catch (e: Exception) {
            println("Error:$e")
        }

    }

}