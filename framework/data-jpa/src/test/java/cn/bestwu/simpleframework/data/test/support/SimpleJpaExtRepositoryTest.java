package cn.bestwu.simpleframework.data.test.support;

import cn.bestwu.simpleframework.data.test.domain.User;
import cn.bestwu.simpleframework.data.test.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Peter Wu
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class SimpleJpaExtRepositoryTest {


  @Autowired
  UserRepository repository;
  final List<User> batch = new ArrayList<>();
  final List<Integer> batchIds = new ArrayList<>();
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

    Collections.addAll(batch, dave, dave1);
    daveId = dave.getId();
    Collections.addAll(batchIds, daveId, dave1.getId());

    repository.delete(dave);
    carterId = carter.getId();

  }

  @After
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
    Assert.assertTrue(optionalUser.isPresent());
  }

  @Test
  public void deleteById() {
    repository.deleteById(carterId);
    Optional<User> optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    Assert.assertTrue(optionalUser.isPresent());
    optionalUser = repository.findById(carterId);
    optionalUser.ifPresent(System.out::println);
    Assert.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void deleteFromRecycleBin() {
    repository.deleteById(carterId);
    Optional<User> optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    Assert.assertTrue(optionalUser.isPresent());
    repository.deleteFromRecycleBin(carterId);
    optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    Assert.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void deleteFromRecycleBin2() {
    repository.deleteById(carterId);
    Optional<User> optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    Assert.assertTrue(optionalUser.isPresent());
    User user = new User();
    user.setId(carterId);
    repository.deleteFromRecycleBin(Example.of(user));
    optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    Assert.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void delete() {
    Optional<User> optionalUser = repository.findByIdFromRecycleBin(daveId);
    optionalUser.ifPresent(System.out::println);
    Assert.assertTrue(optionalUser.isPresent());
    optionalUser = repository.findById(daveId);
    optionalUser.ifPresent(System.out::println);
    Assert.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void deleteInBatch() {
    repository.deleteInBatch(batch);

    List<User> recycleAll = repository.findAllFromRecycleBin();
    System.err.println(recycleAll);
    Assert.assertEquals(2, recycleAll.size());

    repository.deleteInBatch(Collections.emptyList());

    recycleAll = repository.findAllFromRecycleBin();
    System.err.println(recycleAll);
    Assert.assertEquals(2, recycleAll.size());
    recycleAll = repository.findAll();
    System.err.println(recycleAll);
    Assert.assertEquals(2, recycleAll.size());
  }

  @Test
  public void deleteAllInBatch() {
    repository.deleteAllInBatch();

    List<User> recycleAll = repository.findAllFromRecycleBin();
    System.err.println(recycleAll);
    Assert.assertEquals(4, recycleAll.size());
    recycleAll = repository.findAll();
    System.err.println(recycleAll);
    Assert.assertEquals(0, recycleAll.size());
  }

  @Test
  public void existsById() {
    boolean exists = repository.existsById(daveId);
    System.err.println(exists);
    Assert.assertFalse(exists);

    exists = repository.existsById(carterId);
    System.err.println(exists);
    Assert.assertTrue(exists);
  }

  @Test
  public void count() {
    long count = repository.count();
    System.err.println(count);
    Assert.assertEquals(3, count);
    Assert.assertEquals(1, repository.countRecycleBin());
  }

  @Test
  public void findAllById() {
    List<User> users = repository.findAllById(batchIds);
    System.err.println(users);
    Assert.assertEquals(1, users.size());
    Assert.assertEquals(1, repository.countRecycleBin());
  }

  @Test
  public void findById() {
    Optional<User> optionalUser = repository.findById(carterId);
    optionalUser.ifPresent(System.out::println);
    Assert.assertTrue(optionalUser.isPresent());
    optionalUser = repository.findById(daveId);
    optionalUser.ifPresent(System.out::println);
    Assert.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void getOne() {
    User optionalUser = repository.getOne(carterId);
    Assert.assertNotNull(optionalUser);
    optionalUser = repository.getOne(daveId);
    Assert.assertNull(optionalUser);
  }


  @Test
  public void findAll() {
    Assert.assertEquals(3, repository.findAll().size());
  }

  @Test
  public void findAll1() {
    Assert.assertEquals("Dave", repository.findAll(Sort.by("id")).get(0).getFirstname());
    Assert.assertEquals("Carter", repository.findAll(Sort.by("firstname")).get(0).getFirstname());
  }

  @Test
  public void findAll2() {
    Assert.assertEquals(2, repository.findAll(PageRequest.of(0, 2)).getContent().size());
    Assert.assertEquals(3, repository.findAll(PageRequest.of(0, 5)).getContent().size());
  }

  @Test
  public void findOne() {
    Optional<User> one = repository.findOne(Example.of(new User("Dave", null)));
    Assert.assertTrue(one.isPresent());
  }

  @Test
  public void findAll3() {
    Assert.assertEquals(1, repository.findAll(Example.of(new User("Dave", null))).size());
  }

  @Test
  public void count1() {
    Assert.assertEquals(1, repository.count(Example.of(new User("Dave", null))));
  }

  @Test
  public void count2() {
    Assert.assertEquals(3, repository.count());
  }

  @Test
  public void countRecycle() {
    Assert.assertEquals(1, repository.countRecycleBin());
  }

  @Test
  public void countRecycle2() {
    Assert.assertEquals(1, repository.countRecycleBin(Example.of(new User("Dave", null))));
  }

  @Test
  public void findRecycleAll() {
    Assert.assertEquals(1, repository.findAllFromRecycleBin().size());
  }

  @Test
  public void findRecycleById() {
    Assert.assertTrue(repository.findByIdFromRecycleBin(daveId).isPresent());
  }

  @Test
  public void findRecycleOne() {
    Assert.assertTrue(
        repository.findOneFromRecycleBin(Example.of(new User("Dave", null))).isPresent());
  }

  @Test
  public void findRecycleAll1() {
    Assert.assertTrue(
        repository.findAllFromRecycleBin(Example.of(new User("Dave", null))).iterator().hasNext());
  }

  @Test
  public void existsRecycle() {
    Assert.assertTrue(repository.existsInRecycleBin(Example.of(new User("Dave", null))));
  }
}