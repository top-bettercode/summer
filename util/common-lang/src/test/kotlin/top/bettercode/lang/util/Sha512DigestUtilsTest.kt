package top.bettercode.lang.util

import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
internal class Sha512DigestUtilsTest {

    @Test
    fun shaHex() {
        System.err.println(Sha512DigestUtils.shaHex("552ab01dc30855d207f545bd881c455fPOST/outStores/create"))
        System.err.println(Sha512DigestUtils.shaHex("552ab01dc30855d207f545bd881c455fPOST/outStores/create"))
    }
}