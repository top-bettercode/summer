package top.bettercode.summer.tools.lang.util

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.Sha512DigestUtils.shaHex

/**
 * @author Peter Wu
 */
class Sha512DigestUtils1Test {
    @Test
    fun shaHex() {
        System.err.println(
                shaHex("552ab01dc30855d207f545bd881c455fPOST/outStores/create"))
        System.err.println(
                shaHex("552ab01dc30855d207f545bd881c455fPOST/outStores/create"))
        System.err.println(
                shaHex("552ab01dc30855d207f545bd881c455fPOST/outStores/create"))
    }
}
