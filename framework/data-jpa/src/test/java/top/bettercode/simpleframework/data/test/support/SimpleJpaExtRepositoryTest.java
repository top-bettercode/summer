package top.bettercode.simpleframework.data.test.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.data.jpa.query.DefaultSpecMatcher;
import top.bettercode.simpleframework.data.jpa.query.SpecMatcher;
import top.bettercode.simpleframework.data.test.domain.User;
import top.bettercode.simpleframework.data.test.repository.UserRepository;

/**
 * @author Peter Wu
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SimpleJpaExtRepositoryTest {

  @Autowired
  EntityManager entityManager;


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
    User carter = new User("Carter", "Beauford1");
    repository.save(carter);
    carter = new User("Carter", "Beauford2");
    repository.save(carter);

    Collections.addAll(batch, dave, dave1);
    daveId = dave.getId();
    Collections.addAll(batchIds, daveId, dave1.getId());

    repository.delete(dave);
    carterId = carter.getId();
    System.err.println("--------------------------------------------------------");
  }

  @AfterEach
  public void tearDown() {
    System.err.println("--------------------------------------------------------");
    repository.deleteAll();
    repository.cleanRecycleBin();
  }

  @Test
  public void findByFirstName() {
    Page<User> users = repository
        .findByFirstName("Carter", PageRequest.of(0, 1, Sort.by(Direction.ASC, "lastName")));
    System.err.println(users);
    Assertions.assertEquals(2, users.getTotalElements());
    Assertions.assertEquals(1, users.getContent().size());
    Assertions
        .assertEquals("Beauford1", users.getContent().get(0).getLastName());
    users = repository
        .findByFirstName("Carter", PageRequest.of(0, 1, Sort.by(Direction.DESC, "lastName")));
    System.err.println(users);
    Assertions.assertEquals(2, users.getTotalElements());
    Assertions.assertEquals(1, users.getContent().size());
    Assertions
        .assertEquals("Beauford2", users.getContent().get(0).getLastName());
    users = repository
        .findByFirstName("Carter", PageRequest.of(1, 1, Sort.by(Direction.DESC, "lastName")));
    System.err.println(users);
    Assertions.assertEquals(2, users.getTotalElements());
    Assertions.assertEquals(1, users.getContent().size());
    Assertions
        .assertEquals("Beauford1", users.getContent().get(0).getLastName());
  }

  @Test
  void selectNativeSql() {
    repository.selectNativeSql("Carter", PageRequest.of(0, 2)).getContent()
        .forEach(System.out::println);
  }

  @Test
  public void saveSpec() {
    User update = new User();
    update.setLastName("newName");
    DefaultSpecMatcher<User> spec = DefaultSpecMatcher.<User>matching()
        .equal("firstName", "Carter");
    List<User> all = repository.findAll(spec);
    System.err.println(StringUtil.valueOf(all, true));
    repository.save(update, spec);
    all = repository.findAll(spec);
    System.err.println(StringUtil.valueOf(all, true));
    for (User user : all) {
      Assertions.assertEquals("newName", user.getLastName());
    }
  }

  @Test
  public void save1() {
    User dave = new User("Dave", "Matthews");
    repository.save(dave);
    Integer id = dave.getId();
    User update = new User();
    update.setId(id);
    update.setFirstName("Dave22");
    repository.save(update);
    Optional<User> optionalUser = repository.findById(id);
    optionalUser.ifPresent(System.err::println);
    Assertions.assertTrue(optionalUser.isPresent());
  }

  @Deprecated
  @Test
  public void save() {
    User dave = new User("Dave", "Matthews");
    repository.save(dave);
    Integer id = dave.getId();
    User update = new User();
    update.setId(id);
    update.setFirstName("Dave22");
    repository.dynamicSave(update);
    Optional<User> optionalUser = repository.findById(id);
    optionalUser.ifPresent(System.err::println);
    Assertions.assertTrue(optionalUser.isPresent());
  }

  @Test
  public void deleteById() {
    repository.deleteById(carterId);
    Optional<User> optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    Assertions.assertTrue(optionalUser.isPresent());
    optionalUser = repository.findById(carterId);
    optionalUser.ifPresent(System.out::println);
    Assertions.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void deleteFromRecycleBin() {
    repository.deleteById(carterId);
    Optional<User> optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    Assertions.assertTrue(optionalUser.isPresent());
    repository.deleteFromRecycleBin(carterId);
    optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    Assertions.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void deleteFromRecycleBin2() {
    repository.deleteById(carterId);
    Optional<User> optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    Assertions.assertTrue(optionalUser.isPresent());
    repository.deleteFromRecycleBin(DefaultSpecMatcher.<User>matching().equal("id", carterId));
    optionalUser = repository.findByIdFromRecycleBin(carterId);
    optionalUser.ifPresent(System.out::println);
    Assertions.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void delete() {
    Optional<User> optionalUser = repository.findByIdFromRecycleBin(daveId);
    optionalUser.ifPresent(System.out::println);
    Assertions.assertTrue(optionalUser.isPresent());
    optionalUser = repository.findById(daveId);
    optionalUser.ifPresent(System.out::println);
    Assertions.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void deleteInBatch() {
    repository.deleteAllInBatch(batch);

    List<User> recycleAll = repository.findAllFromRecycleBin();
    System.err.println(recycleAll);
    Assertions.assertEquals(2, recycleAll.size());

    repository.deleteAllInBatch(Collections.emptyList());

    recycleAll = repository.findAllFromRecycleBin();
    System.err.println(recycleAll);
    Assertions.assertEquals(2, recycleAll.size());
    recycleAll = repository.findAll();
    System.err.println(recycleAll);
    Assertions.assertEquals(2, recycleAll.size());
  }

  @Test
  public void deleteAllInBatch() {
    repository.deleteAllInBatch();

    List<User> recycleAll = repository.findAllFromRecycleBin();
    System.err.println(StringUtil.valueOf(recycleAll, true));
    Assertions.assertEquals(4, recycleAll.size());
    recycleAll = repository.findAll();
    System.err.println(recycleAll);
    Assertions.assertEquals(0, recycleAll.size());
  }

  @Test
  public void existsById() {
    boolean exists = repository.existsById(daveId);
    System.err.println(exists);
    Assertions.assertFalse(exists);

    exists = repository.existsById(carterId);
    System.err.println(exists);
    Assertions.assertTrue(exists);
  }

  @Test
  public void count() {
    long count = repository.count();
    System.err.println(count);
    Assertions.assertEquals(3, count);
    Assertions.assertEquals(1, repository.countRecycleBin());
  }


  @Test
  public void findByPage() {
    Page<User> users = repository.findAll(PageRequest.of(0, 1));
//    Page<User> users = repository.findAll(Pageable.unpaged());
    for (User user : users) {
      System.err.println(user);
    }
    System.err.println("===========" + users.getTotalElements());
    System.err.println("===========" + users.getContent().size());
    Assertions.assertEquals(3, users.getTotalElements());
    Assertions.assertEquals(1, users.getContent().size());
  }

  @Test
  public void findAllById() {
    List<User> users = repository.findAllById(batchIds);
    System.err.println(users);
    Assertions.assertEquals(1, users.size());
    Assertions.assertEquals(1, repository.countRecycleBin());
  }

  @Test
  public void findById() {
    Optional<User> optionalUser = repository.findById(carterId);
    optionalUser.ifPresent(System.out::println);
    Assertions.assertTrue(optionalUser.isPresent());
    optionalUser = repository.findById(daveId);
    optionalUser.ifPresent(System.out::println);
    Assertions.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void getById() {
    User optionalUser = repository.getById(carterId);
    Assertions.assertNotNull(optionalUser);
    optionalUser = repository.getById(daveId);
    Assertions.assertNull(optionalUser);
  }


  @Test
  public void findAll() {
    Assertions.assertEquals(3, repository.findAll().size());
  }

  @Test
  public void findAll1() {
    Assertions
        .assertEquals("Dave", repository.findAll(Sort.by("id")).get(0).getFirstName());
    Assertions
        .assertEquals("Carter", repository.findAll(Sort.by("firstName")).get(0).getFirstName());
  }

  @Test
  public void findAll2() {
    Specification<User> spec = null;
//    Assertions .assertEquals(2, repository.findAll(PageRequest.of(0, 2)).getContent().size());
//    Assertions .assertEquals(3, repository.findAll(PageRequest.of(0, 5)).getContent().size());
//    Assertions .assertEquals(2, repository.findAll(spec,PageRequest.of(0, 2)).getContent().size());
//    Assertions .assertEquals(3, repository.findAll(spec,PageRequest.of(0, 5)).getContent().size());
    Assertions.assertEquals(2, repository.findAll(2).size());
    Assertions.assertEquals(3, repository.findAll(5).size());
  }

  @Test
  public void findAll33() {
    DefaultSpecMatcher<User> spec = DefaultSpecMatcher.<User>matching()
        .desc("firstName").asc("lastName");
    List<User> all = repository.findAll(spec);
    System.err.println(StringUtil.valueOf(all, true));
  }

  @Test
  public void findAll34() {
    DefaultSpecMatcher<User> spec = DefaultSpecMatcher.<User>matching().equal("id", carterId)
        .containing("firstName", " Cart ").specPath("firstName").trim()
        .desc("firstName").asc("lastName");
    List<User> all = repository.findAll(spec);
    System.err.println(StringUtil.valueOf(all, true));
  }


  @Test
  public void findAll35() {
    SpecMatcher<User, DefaultSpecMatcher<User>> matcher = DefaultSpecMatcher.<User>matching()
        .specPath("lastName").containing("Beauford")
        .any(specMatcher ->
            specMatcher.equal("id", carterId)
                .containing("firstName", " Cart ").specPath("firstName").trim())
        .desc("firstName").asc("lastName");
    List<User> all = repository.findAll(matcher);
    System.err.println(StringUtil.valueOf(all, true));
  }


  @Test
  public void findOne() {
    Optional<User> one = repository.findOne(Example.of(new User("Dave", null)));
    Assertions.assertTrue(one.isPresent());
  }

  @Test
  public void findAll3() {
    Assertions
        .assertEquals(1, repository.findAll(Example.of(new User("Dave", null))).size());
  }

  @Test
  public void count1() {
    Assertions
        .assertEquals(1, repository.count(Example.of(new User("Dave", null))));
  }

  @Test
  public void count2() {
    Assertions.assertEquals(3, repository.count());
  }

  @Test
  public void countRecycle() {
    Assertions.assertEquals(1, repository.countRecycleBin());
  }

  @Test
  public void countRecycle2() {
    Assertions
        .assertEquals(1, repository.countRecycleBin(
            DefaultSpecMatcher.<User>matching().equal("firstName", "Dave")));
  }

  @Test
  public void findRecycleAll() {
    Assertions.assertEquals(1, repository.findAllFromRecycleBin().size());
  }

  @Test
  public void findRecycleById() {
    Assertions
        .assertTrue(repository.findByIdFromRecycleBin(daveId).isPresent());
  }

  @Test
  public void findRecycleOne() {
    Assertions.assertTrue(
        repository.findOneFromRecycleBin(
            DefaultSpecMatcher.<User>matching().equal("firstName", "Dave")).isPresent());
  }

  @Test
  public void findRecycleAll1() {
    Assertions.assertTrue(
        repository.findAllFromRecycleBin((root, query, builder) -> builder
            .equal(root.get("firstName"), "Dave")).iterator().hasNext());
  }

  @Test
  public void existsRecycle() {
    Assertions
        .assertTrue(repository.existsInRecycleBin((root, query, builder) -> builder
            .equal(root.get("firstName"), "Dave")));
  }

  @SuppressWarnings({"rawtypes"})
  @Test
  void nativeQuery() {
    Query query = repository.getEntityManager().createNativeQuery(
        "select first_name,last_name, '2022-03-23 16:45:37' as date from t_user where first_name = ? AND first_name = ? and last_name = ?",
        Tuple.class);
    query.setParameter(1, "Carter");
    query.setParameter(2, "Carter");
    query.setParameter(3, "Beauford1");
    NativeQuery nativeQuery = query.unwrap(NativeQuery.class);
//    nativeQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
//    nativeQuery.addScalar("first_name", StringType.INSTANCE);
//    nativeQuery.addScalar("last_name", StringType.INSTANCE);
//    nativeQuery.addScalar("date", TimestampType.INSTANCE);
//    nativeQuery.addScalar("date1", TimestampType.INSTANCE);
//    nativeQuery.stream().forEach(o -> {
//      System.err.println(StringUtil.valueOf(o, true));
//    });
    List resultList = query.getResultList();
    System.err.println(StringUtil.valueOf(resultList, true));
  }
}