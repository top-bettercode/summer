package top.bettercode.summer.logging

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseWebNoAuthTest

/**
 *
 * @author Peter Wu
 */
class AsyncServiceTest : BaseWebNoAuthTest(){

    @Autowired
    lateinit var asyncService: AsyncService

    @Test
    fun async() {
        asyncService.async()
        Thread.sleep(5000)
    }

    @Test
    fun scheduled() {
        asyncService.scheduled()
        Thread.sleep(5000)
    }
}