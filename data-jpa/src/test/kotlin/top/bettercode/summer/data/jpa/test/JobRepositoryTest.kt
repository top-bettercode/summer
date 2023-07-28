package top.bettercode.summer.data.jpa.test

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import top.bettercode.summer.data.jpa.domain.Job
import top.bettercode.summer.data.jpa.repository.JobRepository
import javax.persistence.EntityManager

/**
 * @author Peter Wu
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest
class JobRepositoryTest {
    @Autowired
    var entityManager: EntityManager? = null

    @Autowired
    var jobRepository: JobRepository? = null

    @BeforeEach
    fun setUp() {
        System.err.println("--------------------------------------------------------")
    }

    @AfterEach
    fun tearDown() {
        System.err.println("--------------------------------------------------------")
    }

    @Test
    fun save() {
        val job = Job()
        job.id = ""
        job.name = "IT"
        jobRepository!!.save(job)
        System.err.println(job)
    }
}
