package top.bettercode.summer.data.jpa.test

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import top.bettercode.summer.data.jpa.domain.PhysicalUser
import top.bettercode.summer.data.jpa.domain.User
import top.bettercode.summer.data.jpa.repository.PhysicalUserRepository
import top.bettercode.summer.data.jpa.repository.UserRepository
import java.util.*
import javax.sql.DataSource

@ExtendWith(SpringExtension::class)
@SpringBootTest
class LogicalDeleteTest {
    @Autowired
    lateinit var repository: UserRepository

    @Autowired
    var physicalUserRepository: PhysicalUserRepository? = null

    @Autowired
    var dataSource: DataSource? = null
    val batch: MutableList<User> = ArrayList()
    val batchIds: MutableList<Int?> = ArrayList()
    var daveId: Int = 0
    var carterId: Int = 0

    @BeforeEach
    fun setUp() {
        val dave = User("Dave", "Matthews")
        repository.save(dave)
        val dave1 = User("Dave", "Matthews")
        repository.save(dave1)
        var carter = User("Carter", "Beauford")
        repository.save(carter)
        carter = User("Carter", "Beauford")
        repository.save(carter)
        Collections.addAll(batch, dave, dave1)
        daveId = dave.id!!
        Collections.addAll(batchIds, daveId, dave1.id)
        repository.delete(dave)
        carterId = carter.id!!
        System.err.println("--------------------------------------------------------")
    }

    @AfterEach
    fun tearDown() {
        System.err.println("--------------------------------------------------------")
        repository.deleteAll()
        repository.cleanRecycleBin()
        physicalUserRepository!!.deleteAll()
        physicalUserRepository!!.cleanRecycleBin()
    }

    @Test
    fun physicalDeleteTest() {
        val dave = PhysicalUser("Dave", "Matthews")
        physicalUserRepository!!.save(dave)
        physicalUserRepository!!.delete(dave)
        val optionalUser = physicalUserRepository!!.findByIdFromRecycleBin(dave.id)
        optionalUser.ifPresent { x: PhysicalUser? -> println(x) }
        Assertions.assertFalse(optionalUser.isPresent)
    }

    @Test
    fun methdQuery() {
        repository.deleteAllInBatch(batch)
        var users = repository.findByLastName("Matthews")
        System.err.println(users)
        Assertions.assertEquals(0, users!!.size)
        users = repository.findByLastName("Beauford")
        System.err.println(users)
        Assertions.assertEquals(2, users!!.size)
        val recycleAll = repository.findAllFromRecycleBin()
        System.err.println(recycleAll)
        Assertions.assertEquals(2, recycleAll.size)
    }

    @Test
    fun methdDelete() {
        repository.deleteByLastName("Matthews")
        var users = repository.findByLastName("Matthews")
        System.err.println(users)
        Assertions.assertEquals(0, users!!.size)
        users = repository.findByLastName("Beauford")
        System.err.println(users)
        Assertions.assertEquals(2, users!!.size)
        val recycleAll = repository.findAllFromRecycleBin()
        System.err.println(recycleAll)
        Assertions.assertEquals(2, recycleAll.size)
    }
}