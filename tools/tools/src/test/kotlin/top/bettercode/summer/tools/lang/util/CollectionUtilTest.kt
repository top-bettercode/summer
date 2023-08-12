package top.bettercode.summer.tools.lang.util

import org.junit.jupiter.api.Test

/**
 * @author Peter Wu
 */
internal class CollectionUtilTest {

    @Test
    fun partition() {
        val list = listOf("1", "2", "3", "4", "5", "6", "7")
        System.err.println(StringUtil.json(CollectionUtil.partition(list, 2), true))
    }

    //test divideList method
    @Test
    fun divideList() {
        val list = listOf("1", "2", "3", "4", "5", "6", "7")
        System.err.println(StringUtil.json(CollectionUtil.divideList(list, 5), true))
    }

}