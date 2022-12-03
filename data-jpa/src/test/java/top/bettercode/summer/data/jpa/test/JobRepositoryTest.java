package top.bettercode.summer.data.jpa.test;

import javax.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import top.bettercode.summer.data.jpa.domain.Job;
import top.bettercode.summer.data.jpa.repository.JobRepository;

/**
 * @author Peter Wu
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class JobRepositoryTest {

  @Autowired
  EntityManager entityManager;

  @Autowired
  JobRepository jobRepository;


  @BeforeEach
  public void setUp() {
    System.err.println("--------------------------------------------------------");
  }

  @AfterEach
  public void tearDown() {
    System.err.println("--------------------------------------------------------");
  }

  @Test
  void save() {
    Job job = new Job();
    job.setId("");
    job.setName("IT");
    jobRepository.save(job);
    System.err.println(job);
  }

}
