package top.bettercode.summer.tools.lang.util

import org.junit.jupiter.api.Test
import java.net.Inet4Address

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