package top.bettercode.lang.util

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*

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