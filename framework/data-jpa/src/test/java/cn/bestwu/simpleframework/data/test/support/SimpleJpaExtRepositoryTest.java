package cn.bestwu.simpleframework.data.test.support;

import cn.bestwu.simpleframework.data.test.domain.User;
import cn.bestwu.simpleframework.data.test.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Peter Wu
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SimpleJpaExtRepositoryTest {


  @Autowired
  UserRepository repository;
  final List<User> batch = new ArrayList<>();
  final List<Integer> batchIds = new ArrayList<>();
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

    Collections.addAll(batch, dave, dave1);
    daveId = dave.getId();
    Collections.addAll(batchIds, daveId, dave1.getId());

    repository.delete(dave);
    carterId = carter.getId();

  }

  @AfterEach
  public void tearDown() {
    repository.deleteAll();
    repository.cleanRecycleBin();
  }

  @Test
  public void save() {
    User dave = new User("Dave", "Matthews");
    repository.save(dave);
    Optional<User> optionalUser = repository.findById(dave.getId());
    optionalUser.ifPresent(System.out::println);
    org.junit.jupiter.api.Assertions.assertTrue(optionalUser.isPresent());
  }

  @Test
  public void deleteById() {
    repository.deleteById(carterId);
    Optional<User> optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    org.junit.jupiter.api.Assertions.assertTrue(optionalUser.isPresent());
    optionalUser = repository.findById(carterId);
    optionalUser.ifPresent(System.out::println);
    org.junit.jupiter.api.Assertions.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void deleteFromRecycleBin() {
    repository.deleteById(carterId);
    Optional<User> optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    org.junit.jupiter.api.Assertions.assertTrue(optionalUser.isPresent());
    repository.deleteFromRecycleBin(carterId);
    optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    org.junit.jupiter.api.Assertions.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void deleteFromRecycleBin2() {
    repository.deleteById(carterId);
    Optional<User> optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    org.junit.jupiter.api.Assertions.assertTrue(optionalUser.isPresent());
    User user = new User();
    user.setId(carterId);
    repository.deleteFromRecycleBin(Example.of(user));
    optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    org.junit.jupiter.api.Assertions.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void delete() {
    Optional<User> optionalUser = repository.findByIdFromRecycleBin(daveId);
    optionalUser.ifPresent(System.out::println);
    org.junit.jupiter.api.Assertions.assertTrue(optionalUser.isPresent());
    optionalUser = repository.findById(daveId);
    optionalUser.ifPresent(System.out::println);
    org.junit.jupiter.api.Assertions.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void deleteInBatch() {
    repository.deleteAllInBatch(batch);

    List<User> recycleAll = repository.findAllFromRecycleBin();
    System.err.println(recycleAll);
    org.junit.jupiter.api.Assertions.assertEquals(2, recycleAll.size());

    repository.deleteAllInBatch(Collections.emptyList());

    recycleAll = repository.findAllFromRecycleBin();
    System.err.println(recycleAll);
    org.junit.jupiter.api.Assertions.assertEquals(2, recycleAll.size());
    recycleAll = repository.findAll();
    System.err.println(recycleAll);
    org.junit.jupiter.api.Assertions.assertEquals(2, recycleAll.size());
  }

  @Test
  public void deleteAllInBatch() {
    repository.deleteAllInBatch();

    List<User> recycleAll = repository.findAllFromRecycleBin();
    System.err.println(recycleAll);
    org.junit.jupiter.api.Assertions.assertEquals(4, recycleAll.size());
    recycleAll = repository.findAll();
    System.err.println(recycleAll);
    org.junit.jupiter.api.Assertions.assertEquals(0, recycleAll.size());
  }

  @Test
  public void existsById() {
    boolean exists = repository.existsById(daveId);
    System.err.println(exists);
    org.junit.jupiter.api.Assertions.assertFalse(exists);

    exists = repository.existsById(carterId);
    System.err.println(exists);
    org.junit.jupiter.api.Assertions.assertTrue(exists);
  }

  @Test
  public void count() {
    long count = repository.count();
    System.err.println(count);
    org.junit.jupiter.api.Assertions.assertEquals(3, count);
    org.junit.jupiter.api.Assertions.assertEquals(1, repository.countRecycleBin());
  }

  @Test
  public void findAllById() {
    List<User> users = repository.findAllById(batchIds);
    System.err.println(users);
    org.junit.jupiter.api.Assertions.assertEquals(1, users.size());
    org.junit.jupiter.api.Assertions.assertEquals(1, repository.countRecycleBin());
  }

  @Test
  public void findById() {
    Optional<User> optionalUser = repository.findById(carterId);
    optionalUser.ifPresent(System.out::println);
    org.junit.jupiter.api.Assertions.assertTrue(optionalUser.isPresent());
    optionalUser = repository.findById(daveId);
    optionalUser.ifPresent(System.out::println);
    org.junit.jupiter.api.Assertions.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void getById() {
    User optionalUser = repository.getById(carterId);
    org.junit.jupiter.api.Assertions.assertNotNull(optionalUser);
    optionalUser = repository.getById(daveId);
    org.junit.jupiter.api.Assertions.assertNull(optionalUser);
  }


  @Test
  public void findAll() {
    org.junit.jupiter.api.Assertions.assertEquals(3, repository.findAll().size());
  }

  @Test
  public void findAll1() {
    org.junit.jupiter.api.Assertions
        .assertEquals("Dave", repository.findAll(Sort.by("id")).get(0).getFirstname());
    org.junit.jupiter.api.Assertions
        .assertEquals("Carter", repository.findAll(Sort.by("firstname")).get(0).getFirstname());
  }

  @Test
  public void findAll2() {
    org.junit.jupiter.api.Assertions
        .assertEquals(2, repository.findAll(PageRequest.of(0, 2)).getContent().size());
    org.junit.jupiter.api.Assertions
        .assertEquals(3, repository.findAll(PageRequest.of(0, 5)).getContent().size());
  }

  @Test
  public void findOne() {
    Optional<User> one = repository.findOne(Example.of(new User("Dave", null)));
    org.junit.jupiter.api.Assertions.assertTrue(one.isPresent());
  }

  @Test
  public void findAll3() {
    org.junit.jupiter.api.Assertions
        .assertEquals(1, repository.findAll(Example.of(new User("Dave", null))).size());
  }

  @Test
  public void count1() {
    org.junit.jupiter.api.Assertions
        .assertEquals(1, repository.count(Example.of(new User("Dave", null))));
  }

  @Test
  public void count2() {
    org.junit.jupiter.api.Assertions.assertEquals(3, repository.count());
  }

  @Test
  public void countRecycle() {
    org.junit.jupiter.api.Assertions.assertEquals(1, repository.countRecycleBin());
  }

  @Test
  public void countRecycle2() {
    org.junit.jupiter.api.Assertions
        .assertEquals(1, repository.countRecycleBin(Example.of(new User("Dave", null))));
  }

  @Test
  public void findRecycleAll() {
    org.junit.jupiter.api.Assertions.assertEquals(1, repository.findAllFromRecycleBin().size());
  }

  @Test
  public void findRecycleById() {
    org.junit.jupiter.api.Assertions
        .assertTrue(repository.findByIdFromRecycleBin(daveId).isPresent());
  }

  @Test
  public void findRecycleOne() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findOneFromRecycleBin(Example.of(new User("Dave", null))).isPresent());
  }

  @Test
  public void findRecycleAll1() {
    org.junit.jupiter.api.Assertions.assertTrue(
        repository.findAllFromRecycleBin(Example.of(new User("Dave", null))).iterator().hasNext());
  }

  @Test
  public void existsRecycle() {
    org.junit.jupiter.api.Assertions
        .assertTrue(repository.existsInRecycleBin(Example.of(new User("Dave", null))));
  }
}