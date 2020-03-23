package cn.bestwu.simpleframework.data.test.support;

import cn.bestwu.simpleframework.data.test.domain.QUser;
import cn.bestwu.simpleframework.data.test.domain.User;
import cn.bestwu.simpleframework.data.test.repository.UserRepository;
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Peter Wu
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class QuerydslJpaExtPredicateExecutorTest {

  @Autowired
  UserRepository repository;
  Integer daveId;
  Integer carterId;

  @Before
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

  @After
  public void tearDown() {
    repository.deleteAll();
    repository.cleanRecycleBin();
  }

  @Test
  public void findOne() {
    Optional<User> dave = repository.findOne(QUser.user.firstname.eq("Dave"));
    dave.ifPresent(System.out::println);
    Assert.assertTrue(dave.isPresent());
  }

  @Test
  public void findAll() {
    System.err.println(repository.findAll());
    Iterable<User> carter = repository.findAll(QUser.user.firstname.eq("Carter"));
    Assert.assertTrue(carter.iterator().hasNext());
  }

  @Test
  public void findAll1() {
    Assert.assertTrue(
        repository.findAll(QUser.user.firstname.eq("Carter"), QUser.user.lastname.asc()).iterator()
            .hasNext());
  }

  @Test
  public void findAll2() {
    Assert.assertTrue(
        repository.findAll(QUser.user.firstname.eq("Carter"), Sort.by("id")).iterator().hasNext());
  }

  @Test
  public void findAll3() {
    Assert.assertTrue(repository.findAll(QUser.user.lastname.asc()).iterator().hasNext());
  }

  @Test
  public void findAll4() {
    Assert.assertEquals(1,
        repository.findAll(QUser.user.firstname.eq("Carter"), PageRequest.of(0, 1)).getContent()
            .size());
    Assert.assertEquals(2,
        repository.findAll(QUser.user.firstname.eq("Carter"), PageRequest.of(0, 5)).getContent()
            .size());
  }

  @Test
  public void count() {
    Assert.assertEquals(2, repository.count(QUser.user.firstname.eq("Carter")));
  }

  @Test
  public void exists() {
    Assert.assertTrue(repository.exists(QUser.user.firstname.eq("Carter")));
  }

  @Test
  public void findOneFromRecycleBin() {
    Optional<User> dave = repository.findOneFromRecycleBin(QUser.user.firstname.eq("Dave"));
    dave.ifPresent(System.out::println);
    Assert.assertTrue(dave.isPresent());
  }

  @Test
  public void findAllFromRecycleBin() {
    Assert.assertTrue(
        repository.findAllFromRecycleBin(QUser.user.firstname.eq("Dave")).iterator().hasNext());
  }

  @Test
  public void findAllFromRecycleBin1() {
    Assert.assertTrue(
        repository.findAllFromRecycleBin(QUser.user.firstname.eq("Dave"), QUser.user.lastname.asc())
            .iterator()
            .hasNext());
  }

  @Test
  public void findAllFromRecycleBin2() {
    Assert.assertTrue(
        repository.findAllFromRecycleBin(QUser.user.firstname.eq("Dave"), Sort.by("id")).iterator()
            .hasNext());
  }

  @Test
  public void findAllFromRecycleBin3() {
    Assert.assertTrue(
        repository.findAllFromRecycleBin(QUser.user.lastname.asc()).iterator().hasNext());
  }

  @Test
  public void findAllFromRecycleBin4() {
    repository.deleteById(daveId);
    Assert.assertEquals(1,
        repository.findAllFromRecycleBin(QUser.user.firstname.eq("Dave"), PageRequest.of(0, 1))
            .getContent()
            .size());
    Assert.assertEquals(2,
        repository.findAllFromRecycleBin(QUser.user.firstname.eq("Dave"), PageRequest.of(0, 5))
            .getContent()
            .size());
  }

  @Test
  public void countRecycleBin() {
    Assert.assertEquals(1, repository.countRecycleBin(QUser.user.firstname.eq("Dave")));
  }

  @Test
  public void existsInRecycleBin() {
    Assert.assertTrue(repository.existsInRecycleBin(QUser.user.firstname.eq("Dave")));
    Assert.assertFalse(repository.existsInRecycleBin(QUser.user.firstname.eq("Carter")));
  }
}