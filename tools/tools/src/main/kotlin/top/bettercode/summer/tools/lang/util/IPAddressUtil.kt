package top.bettercode.summer.tools.lang.util

import org.springframework.util.StringUtils
import java.io.IOException
import java.io.InputStreamReader
import java.io.LineNumberReader
import java.net.Inet4Address
import java.net.NetworkInterface
import javax.servlet.http.HttpServletRequest

/**
 * IP工具
 *
 * @author Peter Wu
 */
object IPAddressUtil {

    /**
     * 获取本机IP
     * @return ip
     */
    @JvmStatic
    val inet4Address: String
        get() {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            return interfaces.toList().filter { !it.isLoopback && it.isUp && !it.isVirtual }
                    .minBy { it.index }?.inetAddresses?.toList()
                    ?.filterIsInstance<Inet4Address>()?.firstOrNull()?.let { return it.hostAddress }
                    ?: "127.0.0.1"
        }

    /**
     * 获取客户端IP
     *
     * @param request http请求
     * @return ip
     */
    @JvmStatic
    fun getClientIp(request: HttpServletRequest): String {

        var ip: String? = request.getHeader("X-Forwarded-For")
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("Proxy-Client-IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_CLIENT_IP")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR")
        }
        if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.remoteAddr
        }
        if (ip == null) {
            ip = "unknown"
        }
        return ip
    }

    /**
     * 是否为外网
     *
     * @param ipAddress ip
     * @return 是否为外网
     */
    @JvmStatic
    fun isExtranet(ipAddress: String): Boolean {
        if (!StringUtils.hasText(ipAddress)) {
            throw IllegalArgumentException("ipAddress 不能为空")
        }
        return !ipAddress.matches(
                ("(127\\.0\\.0\\.1)|" + "(localhost)|" + "(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|"
                        + "(172\\.((1[6-9])|(2\\d)|(3[01]))\\.\\d{1,3}\\.\\d{1,3})|"
                        + "(192\\.168\\.\\d{1,3}\\.\\d{1,3})").toRegex()
        )
    }

    /**
     * 获取客户端IP对应网卡的MAC地址
     *
     * @param ipAddress ip
     * @return MAC地址
     */
    @JvmStatic
    fun getMACAddress(ipAddress: String): String {
        var str: String?
        var strMAC = ""
        try {
            val pp = Runtime.getRuntime().exec(arrayOf("nbtstat", "-a", ipAddress))
            val ir = InputStreamReader(pp.inputStream)
            val input = LineNumberReader(ir)
            for (i in 1..99) {
                str = input.readLine()
                if (str != null) {
                    if (str.indexOf("MAC Address") > 1) {
                        strMAC = str.substring(str.indexOf("MAC Address") + 14, str.length)
                        break
                    }
                }
            }
        } catch (ex: IOException) {
            return "Can't Get MAC Address!"
        }

        if (strMAC.length < 17) {
            return "Error!"
        }

        return (strMAC.substring(0, 2) + ":" + strMAC.substring(3, 5) + ":" + strMAC.substring(
                6,
                8
        ) + ":"
                + strMAC.substring(9, 11) + ":" + strMAC.substring(12, 14) + ":" + strMAC
                .substring(15, 17))
    }

}