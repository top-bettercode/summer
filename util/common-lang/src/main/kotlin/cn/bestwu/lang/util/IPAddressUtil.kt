package cn.bestwu.lang.util

import org.springframework.util.StringUtils
import java.io.IOException
import java.io.InputStreamReader
import java.io.LineNumberReader
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
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
    // filters out 127.0.0.1 and inactive interfaces
    val inet4Address: String
        get() {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val iface = interfaces.nextElement()
                    if (iface.isLoopback || !iface.isUp) {
                        continue
                    }

                    val addresses = iface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val addr = addresses.nextElement()
                        if (addr is Inet4Address) {
                            return addr.getHostAddress()
                        }
                    }
                }
            } catch (e: SocketException) {
                throw RuntimeException(e)
            }

            return "127.0.0.1"
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
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("Proxy-Client-IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_CLIENT_IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
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
        return !ipAddress.matches(("(127\\.0\\.0\\.1)|" + "(localhost)|" + "(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|"
                + "(172\\.((1[6-9])|(2\\d)|(3[01]))\\.\\d{1,3}\\.\\d{1,3})|"
                + "(192\\.168\\.\\d{1,3}\\.\\d{1,3})").toRegex())
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
        val macAddress: String
        try {
            val pp = Runtime.getRuntime().exec("nbtstat -a $ipAddress")
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

        macAddress = (strMAC.substring(0, 2) + ":" + strMAC.substring(3, 5) + ":" + strMAC.substring(6, 8) + ":"
                + strMAC.substring(9, 11) + ":" + strMAC.substring(12, 14) + ":" + strMAC
                .substring(15, 17))
        return macAddress
    }

}