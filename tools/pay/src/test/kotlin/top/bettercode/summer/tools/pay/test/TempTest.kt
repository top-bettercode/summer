package top.bettercode.summer.tools.pay.test

import org.junit.jupiter.api.Test
import org.springframework.util.DigestUtils

/**
 *
 * @author Peter Wu
 */
class TempTest {

    @Test
    fun md5() {
        val str = "123456"
        val md5 = DigestUtils.md5DigestAsHex(str.toByteArray())
        println(md5)
    }
}