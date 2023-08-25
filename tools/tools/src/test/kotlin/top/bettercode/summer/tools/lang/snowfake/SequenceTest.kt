package top.bettercode.summer.tools.lang.snowfake

import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
class SequenceTest {
    @Test
    fun next() {
        val nextId = Sequence().nextId()
        System.err.println(nextId)
    }

}