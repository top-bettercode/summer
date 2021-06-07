package top.bettercode.simpleframework.data.test.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import top.bettercode.simpleframework.data.test.domain.QUser;
import top.bettercode.simpleframework.data.test.domain.User;
import top.bettercode.simpleframework.data.test.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Peter Wu
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class QuerydslJpaExtPredicateExecutorTest {

  @Autowired
  UserRepository repository;
  Integer daveId;
  Integer carterId;

  @BeforeEach
  public void setUp() {
    User dave = new User("Dave", "Matthews");
    repository.save(dave);
    User dave1 = new User("Dave", "Matthews");
    repository.save(dave1);
    User carter = new User("Carter", "Beauford");
    repository.save(carter);
    carter = new User("Carter", "Beauford");
    repository.save(carter);

    daveId = dave1.getId();

    repository.delete(dave);
    carterId = carter.getId();

  }

  @AfterEach
  public void tearDown() {
    repository.deleteAll();
    repository.cleanRecycleBin();
  }

  @Test
  public void findOne() {
    Optional<User> dave = repository.findOne(QUser.user.firstname.eq("Dave"));
    dave.ifPresent(System.out::println);
    assertTrue(dave.isPresent());
  }

  @Test
  public void findAll() {
    System.err.println(repository.findAll());
    Iterable<User> carter = repository.findAll(QUser.user.firstname.eq("Carter"));
    org.junit.jupiter.api.Assertions.assertTrue(carter.iterator().hasNext());
  }

  @Test
  public void findAll1() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findAll(QUser.user.firstname.eq("Carter"), QUser.user.lastname.asc()).iterator()
            .hasNext());
  }

  @Test
  public void findAll2() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findAll(QUser.user.firstname.eq("Carter"), Sort.by("id")).iterator().hasNext());
  }

  @Test
  public void findAll3() {
    org.junit.jupiter.api.Assertions.assertTrue(repository.findAll(QUser.user.lastname.asc()).iterator().hasNext());
  }

  @Test
  public void findAll4() {
    org.junit.jupiter.api.Assertions.assertEquals(1,
        repository.findAll(QUser.user.firstname.eq("Carter"), PageRequest.of(0, 1)).getContent()
            .size());
    org.junit.jupiter.api.Assertions.assertEquals(2,
        repository.findAll(QUser.user.firstname.eq("Carter"), PageRequest.of(0, 5)).getContent()
            .size());
  }

  @Test
  public void count() {
    org.junit.jupiter.api.Assertions.assertEquals(2, repository.count(QUser.user.firstname.eq("Carter")));
  }

  @Test
  public void exists() {
    org.junit.jupiter.api.Assertions.assertTrue(repository.exists(QUser.user.firstname.eq("Carter")));
  }

  @Test
  public void findOneFromRecycleBin() {
    Optional<User> dave = repository.findOneFromRecycleBin(QUser.user.firstname.eq("Dave"));
    dave.ifPresent(System.out::println);
    org.junit.jupiter.api.Assertions.assertTrue(dave.isPresent());
  }

  @Test
  public void findAllFromRecycleBin() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findAllFromRecycleBin(QUser.user.firstname.eq("Dave")).iterator().hasNext());
  }

  @Test
  public void findAllFromRecycleBin1() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findAllFromRecycleBin(QUser.user.firstname.eq("Dave"), QUser.user.lastname.asc())
            .iterator()
            .hasNext());
  }

  @Test
  public void findAllFromRecycleBin2() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findAllFromRecycleBin(QUser.user.firstname.eq("Dave"), Sort.by("id")).iterator()
            .hasNext());
  }

  @Test
  public void findAllFromRecycleBin3() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findAllFromRecycleBin(QUser.user.lastname.asc()).iterator().hasNext());
  }

  @Test
  public void findAllFromRecycleBin4() {
    repository.deleteById(daveId);
    org.junit.jupiter.api.Assertions.assertEquals(1,
        repository.findAllFromRecycleBin(QUser.user.firstname.eq("Dave"), PageRequest.of(0, 1))
            .getContent()
            .size());
    org.junit.jupiter.api.Assertions.assertEquals(2,
        repository.findAllFromRecycleBin(QUser.user.firstname.eq("Dave"), PageRequest.of(0, 5))
            .getContent()
            .size());
  }

  @Test
  public void countRecycleBin() {
    org.junit.jupiter.api.Assertions.assertEquals(1, repository.countRecycleBin(QUser.user.firstname.eq("Dave")));
  }

  @Test
  public void existsInRecycleBin() {
    org.junit.jupiter.api.Assertions.assertTrue(repository.existsInRecycleBin(QUser.user.firstname.eq("Dave")));
    org.junit.jupiter.api.Assertions.assertFalse(repository.existsInRecycleBin(QUser.user.firstname.eq("Carter")));
  }
}