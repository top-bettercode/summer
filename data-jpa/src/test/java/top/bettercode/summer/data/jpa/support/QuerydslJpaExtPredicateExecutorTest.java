package top.bettercode.summer.data.jpa.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import top.bettercode.summer.data.jpa.domain.QUser;
import top.bettercode.summer.data.jpa.domain.User;
import top.bettercode.summer.data.jpa.repository.UserRepository;

/**
 * @author Peter Wu
 */
@Disabled
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
    Optional<User> dave = repository.findOne(QUser.user.firstName.eq("Dave"));
    dave.ifPresent(System.out::println);
    assertTrue(dave.isPresent());
  }

  @Test
  public void findAll() {
    System.err.println(repository.findAll());
    Iterable<User> carter = repository.findAll(QUser.user.firstName.eq("Carter"));
    org.junit.jupiter.api.Assertions.assertTrue(carter.iterator().hasNext());
  }

  @Test
  public void findAll1() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findAll(QUser.user.firstName.eq("Carter"), QUser.user.lastName.asc()).iterator()
            .hasNext());
  }

  @Test
  public void findAll2() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findAll(QUser.user.firstName.eq("Carter"), Sort.by("id")).iterator().hasNext());
  }

  @Test
  public void findAll3() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findAll(QUser.user.lastName.asc()).iterator().hasNext());
  }

  @Test
  public void findAll4() {
    org.junit.jupiter.api.Assertions.assertEquals(1,
        repository.findAll(QUser.user.firstName.eq("Carter"), PageRequest.of(0, 1)).getContent()
            .size());
    org.junit.jupiter.api.Assertions.assertEquals(2,
        repository.findAll(QUser.user.firstName.eq("Carter"), PageRequest.of(0, 5)).getContent()
            .size());
  }

  @Test
  public void count() {
    org.junit.jupiter.api.Assertions.assertEquals(2,
        repository.count(QUser.user.firstName.eq("Carter")));
  }

  @Test
  public void exists() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.exists(QUser.user.firstName.eq("Carter")));
  }

  @Test
  public void findOneFromRecycleBin() {
    Optional<User> dave = repository.findOneFromRecycleBin(QUser.user.firstName.eq("Dave"));
    dave.ifPresent(System.out::println);
    org.junit.jupiter.api.Assertions.assertTrue(dave.isPresent());
  }

  @Test
  public void findAllFromRecycleBin() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findAllFromRecycleBin(QUser.user.firstName.eq("Dave")).iterator().hasNext());
  }

  @Test
  public void findAllFromRecycleBin1() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findAllFromRecycleBin(QUser.user.firstName.eq("Dave"), QUser.user.lastName.asc())
            .iterator()
            .hasNext());
  }

  @Test
  public void findAllFromRecycleBin2() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findAllFromRecycleBin(QUser.user.firstName.eq("Dave"), Sort.by("id")).iterator()
            .hasNext());
  }

  @Test
  public void findAllFromRecycleBin3() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findAllFromRecycleBin(QUser.user.lastName.asc()).iterator().hasNext());
  }

  @Test
  public void findAllFromRecycleBin4() {
    repository.deleteById(daveId);
    org.junit.jupiter.api.Assertions.assertEquals(1,
        repository.findAllFromRecycleBin(QUser.user.firstName.eq("Dave"), PageRequest.of(0, 1))
            .getContent()
            .size());
    org.junit.jupiter.api.Assertions.assertEquals(2,
        repository.findAllFromRecycleBin(QUser.user.firstName.eq("Dave"), PageRequest.of(0, 5))
            .getContent()
            .size());
  }

  @Test
  public void countRecycleBin() {
    org.junit.jupiter.api.Assertions.assertEquals(1,
        repository.countRecycleBin(QUser.user.firstName.eq("Dave")));
  }

  @Test
  public void existsInRecycleBin() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.existsInRecycleBin(QUser.user.firstName.eq("Dave")));
    org.junit.jupiter.api.Assertions.assertFalse(
        repository.existsInRecycleBin(QUser.user.firstName.eq("Carter")));
  }
}