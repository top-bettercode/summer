package top.bettercode.lang.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
internal class SnUtilTest {

    @Test
    fun toNum() {
        val n = 1461630453925425153
        val sn = SnUtil.toSn(n)
        val toNum = SnUtil.toNum(sn)
        assertEquals(n,toNum)
        System.err.println(sn)
        System.err.println(toNum)
    }

    @Test
    fun toSn() {
        val sn = "1JYhxNqDgrL"
        val n = SnUtil.toNum(sn)
        val s = SnUtil.toSn(n)
        assertEquals(sn,s)
        System.err.println(sn)
        System.err.println(n)
    }
}