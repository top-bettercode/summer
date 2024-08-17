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
import java.lang.Thread.sleep
import javax.sql.DataSource
import javax.transaction.Transactional

@ExtendWith(SpringExtension::class)
@SpringBootTest
class UserRepositoryTest {
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

    //    @Transactional
    @Test
    fun dynamicSaveTest() {
        var dave = User("Wu", "Matthews")
        dave = repository.save(dave)
        val id = dave.id!!
        var optionalUser = repository.findById(id)
        Assertions.assertTrue(optionalUser.isPresent)
        optionalUser.ifPresent { user: User ->
            System.err.println(user)
            Assertions.assertNotNull(user.firstName)
            Assertions.assertNotNull(user.lastModifiedBy)
        }
        dave = User()
        dave.id = id
        dave.lastName = "MM"
        repository.saveDynamic(dave)
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
        repository.saveDynamic(dave)
    }

    @Transactional
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

    @Test
    fun setFixedFirstnameFor() {
        val dave = User("Wu", "Matthews")
        repository.save(dave)
        repository.setFixedFirstnameFor("Wu2", "Matthews")
    }

    @Test
    fun saveAll() {
        val all = listOf(User("Wu1", "Matthews"), User("Wu2", "Matthews"))
        val result = repository.saveAll(all)
        System.err.println(result)
    }


    @Test
    fun run() {
        repository.run {
            repository.save(User("Wu1", "Matthews"))
            repository.save(User("Wu2", "Matthews"))
        }
    }

    @Test
    fun flush() {
        repository.flush()
    }

    @Test
    fun version() {
        var dave = User("Wu", "Matthews")
        System.err.println(dave.version)
        System.err.println(dave.createdDate)
        System.err.println(dave.lastModifiedDate)
        System.err.println(dave.lastModifiedBy)

        dave = repository.save(dave)
        System.err.println(dave.version)
        val createdDate = dave.createdDate
        System.err.println(createdDate)
        var lastModifiedDate = dave.lastModifiedDate
        System.err.println(lastModifiedDate)
        var lastModifiedBy = dave.lastModifiedBy
        System.err.println(lastModifiedBy)
        Assertions.assertEquals(0, dave.version)
        System.err.println(dave.id)
        sleep(1000)
        Assertions.assertNotNull(dave.id)
        dave.firstName = "Wu2"
        dave = repository.save(dave)
        System.err.println(dave.version)
        System.err.println(dave.createdDate)
        System.err.println(dave.lastModifiedDate)
        System.err.println(dave.lastModifiedBy)
        Assertions.assertEquals(createdDate, dave.createdDate)
        Assertions.assertNotEquals(lastModifiedDate, dave.lastModifiedDate)
        lastModifiedDate = dave.lastModifiedDate
        Assertions.assertNotEquals(lastModifiedBy, dave.lastModifiedBy)
        lastModifiedBy = dave.lastModifiedBy
        Assertions.assertEquals(1, dave.version)
        dave.firstName = "Wu3"
        dave = repository.save(dave)
        System.err.println(dave.version)
        System.err.println(dave.createdDate)
        System.err.println(dave.lastModifiedDate)
        System.err.println(dave.lastModifiedBy)
        Assertions.assertEquals(createdDate, dave.createdDate)
        Assertions.assertNotEquals(lastModifiedDate, dave.lastModifiedDate)
        Assertions.assertNotEquals(lastModifiedBy, dave.lastModifiedBy)
        Assertions.assertEquals(2, dave.version)
    }
}