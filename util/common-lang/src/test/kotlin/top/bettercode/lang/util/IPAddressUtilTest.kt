package top.bettercode.lang.util

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.net.Inet4Address
import java.net.InetAddress

/**
 *
 * @author Peter Wu
 */
internal class IPAddressUtilTest {

    @Test
    fun getInet4Address() {
        System.err.println(IPAddressUtil.inet4Address)
        System.err.println(Inet4Address.getLocalHost().hostAddress)
    }
}