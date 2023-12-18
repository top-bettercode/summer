package top.bettercode.summer.data.jpa.support

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Example
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.test.context.junit.jupiter.SpringExtension
import top.bettercode.summer.data.jpa.domain.User
import top.bettercode.summer.data.jpa.domain.UserMatcher
import top.bettercode.summer.data.jpa.query.DefaultSpecMatcher
import top.bettercode.summer.data.jpa.query.SpecMatcher
import top.bettercode.summer.data.jpa.repository.UserRepository
import top.bettercode.summer.data.jpa.support.PageSize.Companion.size
import top.bettercode.summer.tools.lang.util.StringUtil.json
import java.util.*
import java.util.function.Consumer
import javax.persistence.Tuple
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root
import javax.transaction.Transactional

/**
 * @author Peter Wu
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest
class SimpleJpaExtRepositoryTest {
    @Autowired
    lateinit var repository: UserRepository
    val batch: MutableList<User> = ArrayList()
    val batchIds: MutableList<Int> = ArrayList()
    var daveId: Int = 0
    var carterId: Int = 0

    @BeforeEach
    fun setUp() {
        val dave = User("Dave", "Matthews")
        repository.save(dave)
        val dave1 = User("Dave", "Matthews")
        repository.save(dave1)
        var carter = User("Carter", "Beauford1")
        repository.save(carter)
        carter = User("Carter", "Beauford2")
        repository.save(carter)
        Collections.addAll(batch, dave, dave1)
        daveId = dave.id!!
        batchIds.addAll(arrayListOf(daveId, dave1.id!!))
        repository.delete(dave)
        carterId = carter.id!!
        System.err.println("--------------------------------------------------------")
    }

    @AfterEach
    fun tearDown() {
        System.err.println("--------------------------------------------------------")
        repository.deleteAll()
        repository.deleteAllRecycleBin()
    }

    @Test
    fun findByFirstName() {
        var users = repository
            .findByFirstName(
                "Carter",
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "lastName"))
            )
        System.err.println(users)
        Assertions.assertEquals(2, users!!.totalElements)
        Assertions.assertEquals(1, users.content.size)
        Assertions
            .assertEquals("Beauford1", users.content[0]?.lastName)
        users = repository
            .findByFirstName(
                "Carter",
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "lastName"))
            )
        System.err.println(users)
        Assertions.assertEquals(2, users!!.totalElements)
        Assertions.assertEquals(1, users.content.size)
        Assertions
            .assertEquals("Beauford2", users.content[0]?.lastName)
        users = repository
            .findByFirstName(
                "Carter",
                PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "lastName"))
            )
        System.err.println(users)
        Assertions.assertEquals(2, users!!.totalElements)
        Assertions.assertEquals(1, users.content.size)
        Assertions
            .assertEquals("Beauford1", users.content[0]?.lastName)
    }

    @Test
    fun selectNativeSql() {
        repository.selectNativeSql("Carter", PageRequest.of(0, 2)).content
            .forEach(Consumer { x: User? -> println(x) })
    }

    @Test
    fun saveSpec() {
        val update = User()
        update.lastName = "newName"
        val spec = DefaultSpecMatcher.matching<User>()
            .equal("firstName", "Carter")
        var all = repository.findAll(spec)
        System.err.println(json(all, true))
        repository.update(update, spec)
        all = repository.findAll(spec)
        System.err.println(json(all, true))
        for (user in all) {
            Assertions.assertEquals("newName", user?.lastName)
        }
    }

    @Test
    fun saveSpec2() {
        val spec = DefaultSpecMatcher.matching<User>()
            .equal("firstName", "Carter")
//                .equal("id", 4)
//                .equal("version", 1)
        spec.criteriaUpdate("lastName", "newName")
        var all = repository.findAll(spec)
        System.err.println(json(all, true))
        repository.update(spec)
        all = repository.findAll(spec)
        System.err.println(json(all, true))
        for (user in all) {
            Assertions.assertEquals("newName", user?.lastName)
        }
    }

    @Test
    fun save1() {
        val dave = User("Dave", "Matthews")
        repository.save(dave)
        val id = dave.id!!
        val update = User()
        update.id = id
        update.firstName = "Dave22"
        update.version = dave.version
        repository.save(update)
        val optionalUser = repository.findById(id)
        optionalUser.ifPresent { x: User? -> System.err.println(x) }
        Assertions.assertTrue(optionalUser.isPresent)
    }

    @Deprecated("")
    @Test
    fun save() {
        val dave = User("Dave", "Matthews")
        repository.save(dave)
        val id = dave.id!!
        val update = User()
        update.id = id
        update.firstName = "Dave22"
        repository.saveDynamic(update)
        val optionalUser = repository.findById(id)
        optionalUser.ifPresent { x: User? -> System.err.println(x) }
        Assertions.assertTrue(optionalUser.isPresent)
    }

    @Test
    fun deleteById() {
        repository.deleteById(carterId)
        var optionalUser = repository.findByIdFromRecycleBin(carterId)
        optionalUser.ifPresent { x: User? -> println(x) }
        Assertions.assertTrue(optionalUser.isPresent)
        optionalUser = repository.findById(carterId)
        optionalUser.ifPresent { x: User? -> println(x) }
        Assertions.assertFalse(optionalUser.isPresent)
    }

    @Test
    fun deleteFromRecycleBin() {
        repository.deleteById(carterId)
        var optionalUser = repository.findByIdFromRecycleBin(carterId)
        optionalUser.ifPresent { x: User? -> println(x) }
        Assertions.assertTrue(optionalUser.isPresent)
        repository.deleteFromRecycleBin(carterId)
        optionalUser = repository.findByIdFromRecycleBin(carterId)
        optionalUser.ifPresent { x: User? -> println(x) }
        Assertions.assertFalse(optionalUser.isPresent)
    }

    @Test
    fun deleteFromRecycleBin2() {
        repository.deleteById(carterId)
        var optionalUser = repository.findByIdFromRecycleBin(carterId)
        optionalUser.ifPresent { x: User? -> println(x) }
        Assertions.assertTrue(optionalUser.isPresent)
        repository.deleteFromRecycleBin(DefaultSpecMatcher.matching<User>().equal("id", carterId))
        optionalUser = repository.findByIdFromRecycleBin(carterId)
        optionalUser.ifPresent { x: User? -> println(x) }
        Assertions.assertFalse(optionalUser.isPresent)
    }

    @Test
    fun delete() {
        var optionalUser = repository.findByIdFromRecycleBin(daveId)
        optionalUser.ifPresent { x: User? -> println(x) }
        Assertions.assertTrue(optionalUser.isPresent)
        optionalUser = repository.findById(daveId)
        optionalUser.ifPresent { x: User? -> println(x) }
        Assertions.assertFalse(optionalUser.isPresent)
    }

    @Test
    fun deleteInBatch() {
        repository.deleteInBatch(batch)
        var recycleAll = repository.findAllFromRecycleBin()
        System.err.println(recycleAll)
        Assertions.assertEquals(2, recycleAll.size)
        repository.deleteInBatch(emptyList())
        recycleAll = repository.findAllFromRecycleBin()
        System.err.println(recycleAll)
        Assertions.assertEquals(2, recycleAll.size)
        recycleAll = repository.findAll()
        System.err.println(recycleAll)
        Assertions.assertEquals(2, recycleAll.size)
    }

    @Test
    fun deleteAllInBatch() {
        repository.deleteAllInBatch()
        var recycleAll = repository.findAllFromRecycleBin()
        System.err.println(json(recycleAll, true))
        Assertions.assertEquals(4, recycleAll.size)
        recycleAll = repository.findAll()
        System.err.println(recycleAll)
        Assertions.assertEquals(0, recycleAll.size)
    }

    @Test
    fun existsById() {
        var exists = repository.existsById(daveId)
        System.err.println(exists)
        Assertions.assertFalse(exists)
        exists = repository.existsById(carterId)
        System.err.println(exists)
        Assertions.assertTrue(exists)
    }

    @Test
    fun count() {
        val count = repository.count()
        System.err.println(count)
        Assertions.assertEquals(3, count)
        Assertions.assertEquals(1, repository.countRecycleBin())
    }

    @Test
    fun findByPage() {
        val users = repository.findAll(PageRequest.of(0, 1))
        //    Page<User> users = repository.findAll(Pageable.unpaged());
        for (user in users) {
            System.err.println(user)
        }
        System.err.println("===========" + users.totalElements)
        System.err.println("===========" + users.content.size)
        Assertions.assertEquals(3, users.totalElements)
        Assertions.assertEquals(1, users.content.size)
    }

    @Test
    fun findAllById() {
        val users = repository.findAllById(batchIds)
        System.err.println(users)
        Assertions.assertEquals(1, users.size)
        Assertions.assertEquals(1, repository.countRecycleBin())
    }

    @Test
    fun findById() {
        var optionalUser = repository.findById(carterId)
        optionalUser.ifPresent { x: User? -> println(x) }
        Assertions.assertTrue(optionalUser.isPresent)
        optionalUser = repository.findById(daveId)
        optionalUser.ifPresent { x: User? -> println(x) }
        Assertions.assertFalse(optionalUser.isPresent)
    }

    @Test
    fun testbyId() {
        val optionalUser = repository.getOne(carterId)
        Assertions.assertNotNull(optionalUser)

        Assertions.assertThrows(
            JpaObjectRetrievalFailureException::class.java
        ) {
            repository.getOne(daveId)
        }

    }

    @Test
    fun findAll() {
        Assertions.assertEquals(3, repository.findAll().size)
    }

    @Test
    fun findAll1() {
        Assertions
            .assertEquals("Dave", repository.findAll(Sort.by("id"))[0]?.firstName)
        Assertions
            .assertEquals("Carter", repository.findAll(Sort.by("firstName"))[0]?.firstName)
    }

    @Test
    fun findAll2() {
//        val spec: Specification<User>? = null
        //    Assertions .assertEquals(2, repository.findAll(PageRequest.of(0, 2)).getContent().size());
//    Assertions .assertEquals(3, repository.findAll(PageRequest.of(0, 5)).getContent().size());
//    Assertions .assertEquals(2, repository.findAll(spec,PageRequest.of(0, 2)).getContent().size());
//    Assertions .assertEquals(3, repository.findAll(spec,PageRequest.of(0, 5)).getContent().size());
        Assertions.assertEquals(2, repository.findAll(PageRequest.of(0, 2).size()).size)
        val findAll = repository.findAll(PageRequest.of(0, 5).size())
        Assertions.assertEquals(3, findAll.content.size)
    }

    @Test
    fun findAll33() {
        val spec = DefaultSpecMatcher.matching<User?>()
            .desc("firstName").asc("lastName")
        val all = repository.findAll(spec)
        System.err.println(json(all, true))
    }

    @Test
    fun findAll34() {
        val spec = DefaultSpecMatcher.matching<User?>().equal("id", carterId)
            .containing("firstName", " Cart ").path<String>("firstName").trim()
            .desc("firstName").asc("lastName")
        val all = repository.findAll(spec)
        System.err.println(json(all, true))
    }

    @Test
    fun findAll35() {
        val matcher: SpecMatcher<User?, DefaultSpecMatcher<User?>> =
            DefaultSpecMatcher.matching<User?>()
                .path<String>("lastName").containing("Beauford")
                .any { specMatcher: DefaultSpecMatcher<User?> ->
                    specMatcher.equal("id", carterId)
                        .containing("firstName", " Cart ").path<String>("firstName").trim()
                }
                .desc("firstName").asc("lastName")
        val all = repository.findAll(matcher)
        System.err.println(json(all, true))
    }

    @Test
    fun findAll351() {
        val matcher: SpecMatcher<User?, DefaultSpecMatcher<User?>> =
            DefaultSpecMatcher.matching<User?>()
                .path<String>("lastName").containing("Beauford")
                .all { specMatcher: DefaultSpecMatcher<User?> ->
                    specMatcher.equal("id", carterId)
                        .containing("firstName", " Cart ").path<String>("firstName").trim()
                }
                .desc("firstName").asc("lastName")
        val all = repository.findAll(matcher)
        System.err.println(json(all, true))
    }

    @Test
    fun findAll36() {
        val matcher: UserMatcher =
            UserMatcher.matching().lastName().containing("Beauford")
                .any { matcher: UserMatcher ->
                    matcher.id().equal(carterId).firstName()
                        .containing(" Cart ")
                        .firstName().trim()
                }.firstName().desc().lastName().asc()
                .id().between(1, 6)
                .id().`in`(1, 2, 3, 4, 5, 6)

        val all = repository.findAll(matcher)
        System.err.println(json(all, true))
    }

    @Test
    fun findAll361() {
        val matcher: UserMatcher =
            UserMatcher.matching().lastName().containing("Beauford")
                .all { matcher: UserMatcher ->
                    matcher.id().equal(carterId).firstName()
                        .containing(" Cart ").firstName().trim()
                }.firstName().desc().lastName().asc()
        val all = repository.findAll(matcher)
        System.err.println(json(all, true))
    }

    @Test
    fun findOne() {
        val one = repository.findOne(Example.of(User("Dave", null)))
        Assertions.assertTrue(one.isPresent)
    }

    @Test
    fun findAll3() {
        Assertions
            .assertEquals(1, repository.findAll(Example.of(User("Dave", null))).size)
    }

    @Test
    fun count1() {
        Assertions
            .assertEquals(1, repository.count(Example.of(User("Dave", null))))
    }

    @Test
    fun count2() {
        Assertions.assertEquals(3, repository.count())
    }

    @Test
    fun countRecycle() {
        Assertions.assertEquals(1, repository.countRecycleBin())
    }

    @Test
    fun countRecycle2() {
        Assertions
            .assertEquals(
                1, repository.countRecycleBin(
                    DefaultSpecMatcher.matching<User>().equal("firstName", "Dave")
                )
            )
    }

    @Test
    fun findRecycleAll() {
        Assertions.assertEquals(1, repository.findAllFromRecycleBin().size)
    }

    @Test
    fun findRecycleById() {
        Assertions
            .assertTrue(repository.findByIdFromRecycleBin(daveId).isPresent)
    }

    @Test
    fun findRecycleOne() {
        Assertions.assertTrue(
            repository.findOneFromRecycleBin(
                DefaultSpecMatcher.matching<User>().equal("firstName", "Dave")
            ).isPresent
        )
    }

    @Test
    fun findRecycleAll1() {
        Assertions.assertTrue(
            repository.findAllFromRecycleBin { root: Root<User?>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                builder
                    .equal(root.get<Any>("firstName"), "Dave")
            }.iterator().hasNext()
        )
    }

    @Test
    fun existsRecycle() {
        Assertions
            .assertTrue(repository.existsInRecycleBin { root: Root<User?>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                builder
                    .equal(root.get<Any>("firstName"), "Dave")
            })
    }

    @Test
    fun nativeQuery() {
        val query = repository.entityManager.createNativeQuery(
            "select first_name,last_name, '2022-03-23 16:45:37' as date from t_user where first_name = ? AND first_name = ? and last_name = ?",
            Tuple::class.java
        )
        query.setParameter(1, "Carter")
        query.setParameter(2, "Carter")
        query.setParameter(3, "Beauford1")
        @Suppress("DEPRECATION")
        query.unwrap(org.hibernate.query.Query::class.java)
            .setResultTransformer(org.hibernate.transform.AliasToEntityMapResultTransformer.INSTANCE)

//    nativeQuery.addScalar("first_name", StringType.INSTANCE);
//    nativeQuery.addScalar("last_name", StringType.INSTANCE);
//    nativeQuery.addScalar("date", TimestampType.INSTANCE);
//    nativeQuery.addScalar("date1", TimestampType.INSTANCE);
//    nativeQuery.forEach(o -> {
//      System.err.println(StringUtil.json(o, true));
//    });
        val resultList = query.resultList
        System.err.println(json(resultList, true))
    }

    @Test
    @Transactional
    fun nativeUpdate() {
        val query = repository.entityManager.createNativeQuery(
            "update t_user set first_name = 'abc'  where first_name = ? AND first_name = ? and last_name = ?",
            Tuple::class.java
        )
        query.setParameter(1, "Carter")
        query.setParameter(2, "Carter")
        query.setParameter(3, "Beauford1")
//        val nativeQuery = query.unwrap(NativeQuery::class.java)
        //    nativeQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
//    nativeQuery.addScalar("first_name", StringType.INSTANCE);
//    nativeQuery.addScalar("last_name", StringType.INSTANCE);
//    nativeQuery.addScalar("date", TimestampType.INSTANCE);
//    nativeQuery.addScalar("date1", TimestampType.INSTANCE);
//    nativeQuery.forEach(o -> {
//      System.err.println(StringUtil.json(o, true));
//    });
        val resultList = query.executeUpdate()
        System.err.println(json(resultList, true))
    }

    @Test
    fun specMatcherMethodNames() {
        //SpecMatcher类的公开方法名，按类似 "asc", "desc","path"的方法打印
        val type = SpecMatcher::class.java
        val methods = type.methods
        val s = methods.map { it.name }.filter { !it.contains("$") }.distinct().joinToString {
            "\"${it}\""
        }
        System.err.println(s)
    }
}