package top.bettercode.summer.data.jpa.test

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import top.bettercode.summer.data.jpa.domain.StaticUser
import top.bettercode.summer.data.jpa.domain.User
import top.bettercode.summer.data.jpa.repository.StaticUserRepository
import top.bettercode.summer.data.jpa.repository.UserRepository
import javax.sql.DataSource

@ExtendWith(SpringExtension::class)
@SpringBootTest
class DynamicSaveTest {
    @Autowired
    lateinit var repository: UserRepository

    @Autowired
    var staticUserRepository: StaticUserRepository? = null

    @Autowired
    var dataSource: DataSource? = null

    @BeforeEach
    fun setUp() {
//    RunScript.execute(dataSource.getConnection(),
//        new FileReader(new ClassPathResource("data.sql").getFile()));
        System.err.println("--------------------------------------------------------")
    }

    @AfterEach
    fun tearDown() {
        System.err.println("--------------------------------------------------------")
    }

    @Deprecated("")
    @Test
    fun dynamicSaveTest() {
        var dave = User("Wu", "Matthews")
        dave = repository.save(dave)
        val id = dave.id!!
        var optionalUser = repository.findById(id)
        Assertions.assertTrue(optionalUser.isPresent)
        optionalUser.ifPresent { user: User? ->
            System.err.println(user)
            Assertions.assertNotNull(user?.firstName)
        }
        dave = User()
        dave.id = id
        dave.lastName = "MM"
        repository.dynamicSave(dave)
        optionalUser = repository.findById(id)
        Assertions.assertTrue(optionalUser.isPresent)
        optionalUser.ifPresent { user: User? ->
            System.err.println(user)
            Assertions.assertNotNull(user?.firstName)
        }
    }

    @Test
    fun dynamicSaveForm() {
        val dave = UserForm()
        dave.lastName = "Form"
        repository.dynamicSave(dave)
    }

    @Test
    fun staticSaveTest() {
        var dave = StaticUser(null, "Matthews")
        dave = staticUserRepository!!.save(dave)
        val optionalUser = staticUserRepository!!.findById(dave.id!!)
        Assertions.assertTrue(optionalUser.isPresent)
        optionalUser.ifPresent { user: StaticUser? ->
            System.err.println(user)
            Assertions.assertNull(user?.firstName)
        }
    }
}