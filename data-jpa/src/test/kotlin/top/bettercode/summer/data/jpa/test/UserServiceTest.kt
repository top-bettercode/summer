package top.bettercode.summer.data.jpa.test

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import top.bettercode.summer.data.jpa.domain.User
import top.bettercode.summer.data.jpa.support.UserService
import top.bettercode.summer.tools.lang.util.StringUtil

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
        for (i in 0..4) {
            val dave = User("Dave2", "Matthews")
            userService.save(dave)
        }
        System.err.println("--------------------------------------------------------")
    }

    @AfterEach
    fun tearDown() {
        System.err.println("--------------------------------------------------------")
        userService.deleteAll()
        userService.getRepository().cleanRecycleBin()
    }

    @Test
    fun findSave() {
        userService.findSave()
    }

    @Test
    fun findAllSave() {
        userService.findAllSave()
    }

    @Test
    fun findByFirstNameSave() {
        userService.findByFirstNameSave("Dave2")
    }

    @Test
    fun findMybatisSave() {
        userService.findMybatisSave("Dave2")
    }


    @Test
    fun findMybatisAllSizeSave() {
        userService.findMybatisAllSizeSave()
    }

    @Test
    fun findAllPageByPageDefault() {
        val list = userService.findAllPageByPage {
            userService.findAll(it)
        }
        System.err.println(list.size)
        Assertions.assertEquals(5, list.size)
        System.err.println(StringUtil.json(list,true))
    }

    @Test
    fun findAllPageByPage() {
        val list = userService.findAllPageByPage(1) {
            userService.findAll(it)
        }
        System.err.println(list.size)
        Assertions.assertEquals(5, list.size)
        System.err.println(StringUtil.json(list,true))
        Assertions.assertEquals(list, userService.findAll())
    }
}