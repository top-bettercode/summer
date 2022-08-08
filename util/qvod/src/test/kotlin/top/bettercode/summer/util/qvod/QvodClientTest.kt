package top.bettercode.summer.util.qvod

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.util.test.BaseTest

/**
 *
 * @author Peter Wu
 */
class QvodClientTest : BaseTest() {

    @Autowired
    lateinit var qvodClient: QvodClient

    @Test
    fun signature() {
        val signature = qvodClient.signature()
        System.err.println(signature)
    }

    @Test
    fun upload() {
    }

    @Test
    fun processMedia() {
    }

    @Test
    fun reviewImage() {
    }

    @Test
    fun pullEvents() {
    }

    @Test
    fun confirmEvents() {
    }
}