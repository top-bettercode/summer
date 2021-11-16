package top.bettercode.lang.util

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

/**
 * @author Peter Wu
 */
internal class CollectionUtilTest {

    @Test
    fun cut() {
        val list= listOf("1","2","3","4","5","6","7")
        System.err.println(StringUtil.valueOf(CollectionUtil.cut(list,2),true))
    }
}