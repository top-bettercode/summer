package top.bettercode.summer.data.jpa.support

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.junit.jupiter.SpringExtension
import top.bettercode.summer.data.jpa.domain.QUser
import top.bettercode.summer.data.jpa.domain.User
import top.bettercode.summer.data.jpa.repository.UserRepository
import java.util.*

/**
 * @author Peter Wu
 */
//@Disabled
@ExtendWith(SpringExtension::class)
@SpringBootTest
class QuerydslJpaExtPredicateExecutorTest {
    @Autowired
    lateinit var repository: UserRepository
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
        daveId = dave1.id!!
        repository.delete(dave)
        carterId = carter.id!!
    }

    @AfterEach
    fun tearDown() {
        repository.deleteAll()
        repository.cleanRecycleBin()
    }

    @Test
    fun name() {
        System.err.println(repository.findAll(QUser.user.firstName!!.contains("D")))
    }

    @Test
    fun findOne() {
        val dave: Optional<User?> = repository.findOne(QUser.user.firstName.eq("Dave"))
        dave.ifPresent { x: User -> println(x) }
        Assertions.assertTrue(dave.isPresent)
    }

    @Test
    fun findAll() {
        System.err.println(repository.findAll())
        val carter: Iterable<User?> = repository.findAll(QUser.user.firstName!!.eq("Carter"))
        Assertions.assertTrue(carter.iterator().hasNext())
    }

    @Test
    fun findAll1() {
        Assertions.assertTrue(
                repository.findAll(QUser.user.firstName!!.eq("Carter"), QUser.user.lastName!!.asc()).iterator()
                        .hasNext())
    }

    @Test
    fun findAll2() {
        Assertions.assertTrue(
                repository.findAll(QUser.user.firstName!!.eq("Carter"), Sort.by("id")).iterator().hasNext())
    }

    @Test
    fun findAll3() {
        Assertions.assertTrue(
                repository.findAll(QUser.user.lastName!!.asc()).iterator().hasNext())
    }

    @Test
    fun findAll4() {
        Assertions.assertEquals(1,
                repository.findAll(QUser.user.firstName!!.eq("Carter"), PageRequest.of(0, 1)).content
                        .size)
        Assertions.assertEquals(2,
                repository.findAll(QUser.user.firstName.eq("Carter"), PageRequest.of(0, 5)).content
                        .size)
    }

    @Test
    fun count() {
        Assertions.assertEquals(2,
                repository.count(QUser.user.firstName!!.eq("Carter")))
    }

    @Test
    fun exists() {
        Assertions.assertTrue(
                repository.exists(QUser.user.firstName!!.eq("Carter")))
    }

    @Test
    fun findOneFromRecycleBin() {
        val dave: Optional<User> = repository.findOneFromRecycleBin(QUser.user.firstName!!.eq("Dave"))
        dave.ifPresent { x: User? -> println(x) }
        Assertions.assertTrue(dave.isPresent)
    }

    @Test
    fun findAllFromRecycleBin() {
        Assertions.assertTrue(
                repository.findAllFromRecycleBin(QUser.user.firstName!!.eq("Dave")).iterator().hasNext())
    }

    @Test
    fun findAllFromRecycleBin1() {
        Assertions.assertTrue(
                repository.findAllFromRecycleBin(QUser.user.firstName!!.eq("Dave"), QUser.user.lastName!!.asc())
                        .iterator()
                        .hasNext())
    }

    @Test
    fun findAllFromRecycleBin2() {
        Assertions.assertTrue(
                repository.findAllFromRecycleBin(QUser.user.firstName!!.eq("Dave"), Sort.by("id")).iterator()
                        .hasNext())
    }

    @Test
    fun findAllFromRecycleBin3() {
        Assertions.assertTrue(
                repository.findAllFromRecycleBin(QUser.user.lastName!!.asc()).iterator().hasNext())
    }

    @Test
    fun findAllFromRecycleBin4() {
        repository.deleteById(daveId)
        Assertions.assertEquals(1,
                repository.findAllFromRecycleBin(QUser.user.firstName!!.eq("Dave"), PageRequest.of(0, 1))
                        .content
                        .size)
        Assertions.assertEquals(2,
                repository.findAllFromRecycleBin(QUser.user.firstName.eq("Dave"), PageRequest.of(0, 5))
                        .content
                        .size)
    }

    @Test
    fun countRecycleBin() {
        Assertions.assertEquals(1,
                repository.countRecycleBin(QUser.user.firstName!!.eq("Dave")))
    }

    @Test
    fun existsInRecycleBin() {
        Assertions.assertTrue(
                repository.existsInRecycleBin(QUser.user.firstName!!.eq("Dave")))
        Assertions.assertFalse(
                repository.existsInRecycleBin(QUser.user.firstName.eq("Carter")))
    }
}