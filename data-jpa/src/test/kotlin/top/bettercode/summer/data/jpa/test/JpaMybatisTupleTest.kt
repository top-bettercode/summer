package top.bettercode.summer.data.jpa.test

import org.apache.ibatis.session.SqlSession
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import top.bettercode.summer.data.jpa.domain.User
import top.bettercode.summer.data.jpa.repository.Service
import top.bettercode.summer.data.jpa.repository.UserRepository
import top.bettercode.summer.data.jpa.support.Size
import top.bettercode.summer.tools.lang.util.CollectionUtil.mapOf
import top.bettercode.summer.tools.lang.util.StringUtil.valueOf
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer

@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestPropertySource(properties = ["spring.data.jpa.mybatis.use-tuple-transformer=true"])
class JpaMybatisTupleTest {
    @Autowired
    var service: Service? = null

    @Autowired
    lateinit var repository: UserRepository

    @Autowired
    lateinit var sqlSession: SqlSession
    var carterId: Int = 0
    var s: Long = 0

    @BeforeEach
    fun setUp() {
        var carter = User(null, null)
        repository.save(carter)
        val dave = User("Dave2", "Matthews")
        repository.save(dave)
        val dave1 = User("Dave", "Matthews")
        repository.save(dave1)
        carter = User("Carter", "Beauford1")
        repository.save(carter)
        carter = User("Carter", "Beauford2")
        repository.save(carter)
        carterId = carter.id!!
        repository.delete(dave)
        System.err.println("--------------------------------------------------------")
        s = System.currentTimeMillis()
    }

    @AfterEach
    fun tearDown() {
        System.err.println("token seconds:" + (System.currentTimeMillis() - s))
        System.err.println("--------------------------------------------------------")
        repository.deleteAll()
        repository.cleanRecycleBin()
    }

    @Test
    fun selectResultMap() {
        val users = repository.selectResultMap(User("Carter", null))
        System.err.println(users)
        Assertions.assertEquals(1, users!!.size)
        val users1 = sqlSession
                .selectList<Any?>(UserRepository::class.java.name + ".selectResultMap",
                        User("Carter", null))
        System.err.println(users1)
        Assertions.assertEquals(1, users1.size)
        Assertions.assertIterableEquals(users, users1)
    }

    @Test
    @Disabled
    fun selectResultMap2() {
        val users = repository.selectResultMap2(User("Carter", null))
        System.err.println(valueOf(users, true))
        Assertions.assertEquals(1, users!!.size)
        val users1 = sqlSession
                .selectList<Any?>(UserRepository::class.java.name + ".selectResultMap2",
                        User("Carter", null))
        System.err.println(valueOf(users1, true))
        Assertions.assertEquals(1, users1.size)
        Assertions.assertIterableEquals(users, users1)
    }

    @Test
    fun selectResultMap3() {
        val users = repository.selectResultMap3(User("Carter", null))
        System.err.println(valueOf(users, true))
        Assertions.assertEquals(2, users!!.size)
        val users1 = sqlSession
                .selectList<Any?>(UserRepository::class.java.name + ".selectResultMap3",
                        User("Carter", null))
        System.err.println(valueOf(users1, true))
        Assertions.assertEquals(2, users1.size)
        Assertions.assertIterableEquals(users, users1)
    }

    @Test
    fun selectResultOne3() {
        Assertions.assertThrows(IncorrectResultSizeDataAccessException::class.java
        ) { repository.selectResultOne3(User("Carter", null)) }
    }

    @Test
    fun selectResultFirst3() {
        val users = repository.selectResultFirst3(User("Carter", null))
        System.err.println(valueOf(users, true))
    }

    @Test
    fun selectResultMap3Page() {
        val users = repository.selectResultMap3(User("Carter", null),
                PageRequest.of(0, 10))
        System.err.println(valueOf(users, true))
        Assertions.assertEquals(2, users!!.totalElements)
        val users1 = sqlSession
                .selectList<Any?>(UserRepository::class.java.name + ".selectResultMap3",
                        User("Carter", null))
        System.err.println(valueOf(users1, true))
        Assertions.assertEquals(2, users1.size)
        Assertions.assertIterableEquals(users, users1)
    }

    @Test
    fun selectResultMap3Page2() {
        var users = repository.selectResultMap3(User("Carter", null),
                PageRequest.of(0, 2))
        System.err.println(valueOf(users!!.content, true))
        Assertions.assertEquals(2, users.totalElements)
        val pageable = PageRequest.of(1, 2)
        System.err.println(pageable.offset)
        users = repository.selectResultMap3(User("Carter", null),
                pageable)
        System.err.println(valueOf(users!!.content, true))
    }

    @Test
    fun selectMybatisAll() {
        val users = repository.selectMybatisAll()
        System.err.println(valueOf(users, true))
        val users1 = sqlSession
                .selectList<Any?>(UserRepository::class.java.name + ".selectMybatisAll")
        System.err.println(valueOf(users1, true))
        Assertions.assertIterableEquals(users, users1)
        Assertions.assertEquals(4, users!!.size)
    }

    @Test
    fun selectMybatisMapList() {
        val users = repository.selectMybatisMapList()
        System.err.println(valueOf(users, true))
        val users1 = sqlSession
                .selectList<Any?>(UserRepository::class.java.name + ".selectMybatisMapList")
        System.err.println(valueOf(users1, true))
        Assertions.assertIterableEquals(users, users1)
        Assertions.assertEquals(4, users!!.size)
    }

    @Test
    fun selectMybatisMap() {
        val users = repository.selectMybatisMap()
        System.err.println(valueOf(users, true))
        val users1 = sqlSession
                .selectOne<Any>(UserRepository::class.java.name + ".selectMybatisMap")
        System.err.println(valueOf(users1, true))
        Assertions.assertNull(users1)
    }

    @Test
    fun testService() {
        service!!.testService()
    }

    @Test
    fun selectMybatisAllPage() {
        val users = repository.selectMybatisAll(PageRequest.of(0, 1))
        for (user in users!!) {
            System.err.println(user)
        }
        System.err.println("===========" + users.totalElements)
        System.err.println("===========" + users.content.size)
        Assertions.assertEquals(4, users.totalElements)
        Assertions.assertEquals(1, users.content.size)
    }

    @Test
    fun selectMybatisAllPageAll() {
        val users = repository.selectMybatisAll(Pageable.unpaged())
        for (user in users!!) {
            System.err.println(user)
        }
        System.err.println("===========" + users.totalElements)
        System.err.println("===========" + users.content.size)
        Assertions.assertEquals(4, users.totalElements)
        Assertions.assertEquals(4, users.content.size)
    }

    @Test
    fun selectByMybatisSize() {
        val users = repository.selectByMybatisSize(Size.of(2))
        for (user in users!!) {
            System.err.println(user)
        }
        System.err.println("===========" + users.size)
        Assertions.assertEquals(2, users.size)
    }

    @Test
    fun selectMybatisIfParam() {
        val users = repository.selectMybatisIfParam("Carter", "Beauford1")
        System.err.println(users)
        val users1 = sqlSession
                .selectList<Any?>(UserRepository::class.java.name + ".selectMybatisIfParam",
                        mapOf("firstName", "Carter", "lastName", "Beauford1",
                                "param2", "Beauford1"))
        System.err.println(users1)
        Assertions.assertIterableEquals(users, users1)
        Assertions.assertEquals(1, users!!.size)
    }

    @Test
    @Transactional(readOnly = true)
    fun selectMybatisStream() {
        repository.selectMybatisStream("Carter", null).forEach { x: User? -> System.err.println(x) }
    }

    val countDownLatch = CountDownLatch(200)

    @Test
    fun selectMybatisIfParamAsynchronous() {
        val errors: MutableList<Throwable> = ArrayList()
        for (i in 0..99) {
            Thread {
                try {
                    val users = repository.selectMybatisIfParam("Carter", "Beauford1")
                    System.err.println(users)
                    val users1 = sqlSession
                            .selectList<Any?>(UserRepository::class.java.name + ".selectMybatisIfParam",
                                    mapOf("firstName", "Carter",
                                            "lastName", "Beauford1",
                                            "param2", "Beauford1"))
                    System.err.println(users1)
                    Assertions.assertIterableEquals(users, users1)
                    Assertions.assertEquals(1, users!!.size)
                } catch (e: Throwable) {
                    errors.add(e)
                } finally {
                    countDownLatch.countDown()
                }
            }.start()
            Thread {
                try {
                    val users2 = repository.selectMybatisIfParam("Carter", null)
                    System.err.println(users2)
                    val users21 = sqlSession
                            .selectList<Any?>(UserRepository::class.java.name + ".selectMybatisIfParam",
                                    mapOf(Pair("firstName", "Carter"))
                            )
                    System.err.println(users21)
                    Assertions.assertIterableEquals(users2, users21)
                    Assertions.assertEquals(2, users2!!.size)
                } catch (e: Throwable) {
                    errors.add(e)
                } finally {
                    countDownLatch.countDown()
                }
            }.start()
        }
        countDownLatch.await()
        System.err.println("/////////////////")
        errors.forEach(Consumer { obj: Throwable -> obj.printStackTrace() })
        Assertions.assertEquals(0, errors.size)
    }

    @Test
    fun selectByMybatisMap() {
        val params: MutableMap<String, String> = HashMap()
        params["firstName"] = "Carter"
        params["lastName"] = "Beauford1"
        val users = repository.selectByMybatisMap(params)
        System.err.println(users)
        val users1 = sqlSession
                .selectList<Any?>(UserRepository::class.java.name + ".selectByMybatisMap", params)
        System.err.println(users1)
        Assertions.assertIterableEquals(users, users1)
        Assertions.assertEquals(1, users!!.size)
    }

    @Test
    fun selectByMybatisMapPage() {
        val params: MutableMap<String, String> = HashMap()
        params["firstName"] = "Carter"
        val users = repository
                .selectByMybatisMap(PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "lastName")), params)
        System.err.println(valueOf(users.content, true))
        val users1 = sqlSession
                .selectList<Any>(UserRepository::class.java.name + ".selectByMybatisMap", params)
        System.err.println(valueOf(users1, true))
        Assertions.assertEquals(2, users.totalElements)
        val userList = users.content
        Assertions.assertEquals(2, userList.size)
        Assertions.assertEquals(userList[0]?.lastName, "Beauford1")
        Assertions.assertEquals("Beauford2", repository
                .selectByMybatisMap(PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "lastName")), params)
                .content[0]?.lastName)
        val users2 = repository.selectByMybatisMap(params)
        System.err.println(users2)
        Assertions.assertEquals(2, users2!!.size)
        Assertions.assertEquals("Beauford1", users2[0]?.lastName)
    }

    @Test
    fun selectByMybatisEntity() {
        val users = repository.selectByMybatisEntity(User("Carter", null),
                Pageable.unpaged())
        System.err.println(users)
        Assertions.assertEquals(2, users!!.size)
        val users1 = sqlSession
                .selectList<Any?>(UserRepository::class.java.name + ".selectByMybatisEntity",
                        User("Carter", null))
        System.err.println(users1)
        Assertions.assertEquals(2, users1.size)
        Assertions.assertIterableEquals(users, users1)
    }

    @Test
    fun selectByMybatisSort() {
        var users = repository.selectByMybatisSort("Carter", Sort.by(Sort.Direction.ASC, "lastName"))
        System.err.println(users)
        Assertions.assertEquals(2, users!!.size)
        Assertions.assertEquals(users[0]?.lastName, "Beauford1")
        users = repository.selectByMybatisSort("Carter", Sort.by(Sort.Direction.DESC, "lastName"))
        System.err.println(users)
        Assertions.assertEquals(2, users!!.size)
        Assertions.assertEquals("Beauford2", users[0]?.lastName)
    }

    @Test
    fun selectByMybatisSortPage() {
        var users = repository
                .selectByMybatisSort("Carter", PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "lastName")))
        System.err.println(users)
        System.err.println(users!!.content)
        Assertions.assertEquals(2, users.totalElements)
        Assertions.assertEquals(1, users.content.size)
        Assertions
                .assertEquals("Beauford1", users.content[0]?.lastName)
        users = repository
                .selectByMybatisSort("Carter", PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "lastName")))
        System.err.println(users)
        Assertions.assertEquals(2, users!!.totalElements)
        Assertions.assertEquals(1, users.content.size)
        Assertions.assertEquals(users.content[0]?.lastName, "Beauford1")
        users = repository
                .selectByMybatisSort("Carter", PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "lastName")))
        System.err.println(users)
        Assertions.assertEquals(2, users!!.totalElements)
        Assertions.assertEquals(1, users.content.size)
        Assertions
                .assertEquals("Beauford2", users.content[0]?.lastName)
        users = repository
                .selectByMybatisSort("Carter", PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "lastName")))
        System.err.println(users!!.content)
        Assertions.assertEquals(2, users.totalElements)
        Assertions.assertEquals(1, users.content.size)
        Assertions
                .assertEquals("Beauford1", users.content[0]?.lastName)
    }

    @Test
    fun findOneByMybatis() {
        var user = repository.selectOneByMybatis("Dave")
        System.err.println(user)
        Assertions.assertNotNull(user)
        var user1 = sqlSession
                .selectOne<Any>(UserRepository::class.java.name + ".selectOneByMybatis",
                        "Dave")
        System.err.println(user1)
        Assertions.assertNotNull(user1)
        Assertions.assertEquals(user, user1)
        user = repository.selectOneByMybatis("Dave2")
        System.err.println(user)
        Assertions.assertNull(user)
        user1 = sqlSession
                .selectOne(UserRepository::class.java.name + ".selectOneByMybatis",
                        "Dave2")
        System.err.println(user1)
        Assertions.assertNull(user1)
        Assertions.assertEquals(user, user1)
    }

    @Test
    fun insert() {
        val insert = repository.insert("Wu", "Peter")
        val peter = repository.findByLastName("Peter")
        System.err.println(peter)
        Assertions.assertEquals(1, insert)
        Assertions.assertEquals(1, peter!!.size)
    }

    @Test
    fun update() {
        val update = repository.update(carterId, "Peter")
        val userOptional = repository.findById(carterId)
        Assertions.assertTrue(userOptional.isPresent)
        val peter = userOptional.get()
        System.err.println(peter)
        Assertions.assertEquals(1, update)
        Assertions.assertEquals("Peter", peter.lastName)
    }

    @Test
    fun updateNoReturn() {
        repository.updateNoReturn(carterId, "Peter")
        val userOptional = repository.findById(carterId)
        Assertions.assertTrue(userOptional.isPresent)
        val peter = userOptional.get()
        System.err.println(peter)
        Assertions.assertEquals("Peter", peter.lastName)
    }

    @Test
    fun deleteMybatis() {
        val delete = repository.deleteMybatis(carterId)
        val userOptional = repository.findById(carterId)
        Assertions.assertFalse(userOptional.isPresent)
        Assertions.assertEquals(1, delete)
    }

    @Test
    fun deleteMybatisNoResturn() {
        repository.deleteMybatisNoResturn(carterId)
        val userOptional = repository.findById(carterId)
        Assertions.assertFalse(userOptional.isPresent)
    }
}