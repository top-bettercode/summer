package top.bettercode.summer.data.jpa.test

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import top.bettercode.summer.data.jpa.domain.Employee
import top.bettercode.summer.data.jpa.domain.EmployeeKey
import top.bettercode.summer.data.jpa.domain.User
import top.bettercode.summer.data.jpa.query.DefaultSpecMatcher
import top.bettercode.summer.data.jpa.repository.EmployeeRepository
import top.bettercode.summer.data.jpa.repository.UserRepository
import javax.persistence.EntityManager

/**
 * @author Peter Wu
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest
class EmployeeRepositoryTest {
    @Autowired
    lateinit var entityManager: EntityManager

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository
    var delUserId = 0
    var userId = 0
    lateinit var key: EmployeeKey

    @BeforeEach
    fun setUp() {
        val dave = User("Dave", "Matthews1")
        userRepository.save(dave)
        delUserId = dave.id!!
        val dave1 = User("Dave", "Matthews2")
        userRepository.save(dave1)
        userId = dave1.id!!
        var carter = User("Carter", "Beauford1")
        userRepository.save(carter)
        carter = User("Carter", "Beauford2")
        userRepository.save(carter)
        userRepository.delete(dave)
        var employee = Employee(EmployeeKey(1, 1), "Dave", "Matthews1")
        employeeRepository.save(employee)
        employeeRepository.delete(employee)
        employee = Employee(EmployeeKey(2, 2), "Dave", "Matthews2")
        employeeRepository.save(employee)
        key = employee.employeeKey!!
        var employee1 = Employee(EmployeeKey(1, 1), "Carter", "Beauford1")
        employeeRepository.save(employee1)
        employee1 = Employee(EmployeeKey(2, 2), "Carter", "Beauford2")
        employeeRepository.save(employee1)
        System.err.println("--------------------------------------------------------")
    }

    @AfterEach
    fun tearDown() {
        System.err.println("--------------------------------------------------------")
        userRepository.deleteAll()
        userRepository.deleteAllRecycleBin()
        employeeRepository.deleteAll()
        employeeRepository.deleteAllRecycleBin()
    }

    @Test
    fun delete() {
        val spec = DefaultSpecMatcher.matching<User>().equal("firstName", "Dave")
        userRepository.delete(spec)
        val all = userRepository.findAll(spec)
        Assertions.assertEquals(0, all.size)
        val allFromRecycleBin = userRepository.findAllFromRecycleBin(spec)
        Assertions.assertEquals(2, allFromRecycleBin.size)
    }

    @Test
    fun deleteAllById() {
        userRepository.deleteAllById(setOf(userId))
        val byId = userRepository.findById(userId)
        Assertions.assertFalse(byId.isPresent)
        val allFromRecycleBin = userRepository.findAllByIdFromRecycleBin(setOf(userId))
        Assertions.assertEquals(1, allFromRecycleBin.size)
    }

    @Test
    fun deleteAllById2() {
        employeeRepository.deleteAllById(setOf(key))
        val byId = employeeRepository.findById(key)
        Assertions.assertFalse(byId.isPresent)
        val allFromRecycleBin = employeeRepository.findAllByIdFromRecycleBin(setOf(key))
        Assertions.assertEquals(1, allFromRecycleBin.size)
    }

    @Test
    fun deleteAllByIdFromRecycleBin() {
        userRepository.deleteAllByIdFromRecycleBin(setOf(delUserId))
        val byId = userRepository.findByIdFromRecycleBin(delUserId)
        Assertions.assertFalse(byId.isPresent)
    }

    @Test
    fun deleteAllByIdFromRecycleBin2() {
        employeeRepository.deleteAllById(setOf(key))
        employeeRepository.deleteAllByIdFromRecycleBin(setOf(key))
        val byId = employeeRepository.findByIdFromRecycleBin(key)
        Assertions.assertFalse(byId.isPresent)
    }
}
