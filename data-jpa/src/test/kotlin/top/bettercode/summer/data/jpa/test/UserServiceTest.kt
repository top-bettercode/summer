package top.bettercode.summer.data.jpa.test

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import top.bettercode.summer.data.jpa.domain.User
import top.bettercode.summer.data.jpa.support.UserService

/**
 *
 * @author Peter Wu
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest
class UserServiceTest {
    @Autowired
    lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        for (i in 0..20) {
            val dave = User("Dave2", "Matthews")
            userService.save(dave)
        }

    }

    @Test
    fun findAllPageByPage() {
        val list = userService.findAllPageByPage(10) {
            userService.findAll(it)
        }
        System.err.println(list.size)
        Assertions.assertEquals(21, list.size)
    }
}