package top.bettercode.logging.socket

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.io.*
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL

class SocketTestClient {

    @Test
    @Throws(Exception::class)
    fun request() {
        val url = URL("http://127.0.0.1:4560")
        val con = url.openConnection() as HttpURLConnection
        con.doOutput = true
        con.doInput = true
        //		con.setRequestMethod("GET");


        val writer = OutputStreamWriter(con.outputStream, "GB2312")
        writer.write("</TX>")
        writer.flush()
        writer.close()
        //		con.disconnect();
        val `is` = BufferedReader(InputStreamReader(con.inputStream, "GB18030"))
        val line = `is`.readLine()
        System.err.println(line)
    }

    @Test
    @Throws(Exception::class)
    fun requestByTemplate() {
        val restTemplate = RestTemplate()

        val params = LinkedMultiValueMap<String, String>()
        params.add("requestXml", "</TX>")

        val entity = restTemplate.getForEntity("http://127.0.0.1:4560", String::class.java)

        System.err.println(entity.body)
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.statusCode)

    }

    @Test
    @Throws(Exception::class)
    fun socket() {

        var client: Socket? = null
        var `is`: BufferedReader? = null
        var writer: Writer? = null
        try {
            client = Socket("127.0.0.1", 4560)

            //			writer = new OutputStreamWriter(client.getOutputStream(),"GB2312");
            writer = OutputStreamWriter(client.getOutputStream(), "GB2312")
            writer.write("test from client\n")
            writer.write("</TX>")
            writer.flush()

            client.keepAlive = true
            client.shutdownOutput()

            `is` = BufferedReader(InputStreamReader(client.getInputStream(), "GB18030"))
            val line = `is`.readLine()
            System.err.println(line)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            `is`?.close()
            writer?.close()
            client?.close()
        }
    }
}