package top.bettercode.summer.data.jpa.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;
import kotlin.Pair;
import kotlin.collections.MapsKt;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import top.bettercode.summer.data.jpa.domain.User;
import top.bettercode.summer.data.jpa.repository.Service;
import top.bettercode.summer.data.jpa.repository.UserRepository;
import top.bettercode.summer.data.jpa.resp.AUser;
import top.bettercode.summer.data.jpa.resp.CUser;
import top.bettercode.summer.data.jpa.resp.CUsers;
import top.bettercode.summer.data.jpa.support.Size;
import top.bettercode.summer.tools.lang.util.CollectionUtil;
import top.bettercode.summer.tools.lang.util.StringUtil;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = "spring.data.jpa.mybatis.use-tuple-transformer=true")
public class JpaMybatisTupleTest {

  @Autowired
  Service service;
  @Autowired
  UserRepository repository;
  @Autowired
  SqlSession sqlSession;
  Integer carterId;

  @BeforeEach
  public void setUp() {
    User carter = new User(null, null);
    repository.save(carter);
    User dave = new User("Dave2", "Matthews");
    repository.save(dave);
    User dave1 = new User("Dave", "Matthews");
    repository.save(dave1);
    carter = new User("Carter", "Beauford1");
    repository.save(carter);
    carter = new User("Carter", "Beauford2");
    repository.save(carter);
    carterId = carter.getId();
    repository.delete(dave);
    System.err.println("--------------------------------------------------------");
  }

  @AfterEach
  public void tearDown() {
    System.err.println("--------------------------------------------------------");
    repository.deleteAll();
    repository.cleanRecycleBin();
  }


  @Test
  public void selectResultMap() {
    List<CUser> users = repository.selectResultMap(new User("Carter", null));
    System.err.println(users);
    Assertions.assertEquals(1, users.size());
    List<Object> users1 = sqlSession
        .selectList(UserRepository.class.getName() + ".selectResultMap",
            new User("Carter", null));
    System.err.println(users1);
    Assertions.assertEquals(1, users1.size());
    Assertions.assertIterableEquals(users, users1);
  }

  @Test
  @Disabled
  public void selectResultMap2() {
    List<CUsers> users = repository.selectResultMap2(new User("Carter", null));
    System.err.println(StringUtil.valueOf(users, true));
    Assertions.assertEquals(1, users.size());
    List<Object> users1 = sqlSession
        .selectList(UserRepository.class.getName() + ".selectResultMap2",
            new User("Carter", null));
    System.err.println(StringUtil.valueOf(users1, true));
    Assertions.assertEquals(1, users1.size());
    Assertions.assertIterableEquals(users, users1);
  }

  @Test
  public void selectResultMap3() {
    List<AUser> users = repository.selectResultMap3(new User("Carter", null));
    System.err.println(StringUtil.valueOf(users, true));
    Assertions.assertEquals(2, users.size());
    List<Object> users1 = sqlSession
        .selectList(UserRepository.class.getName() + ".selectResultMap3",
            new User("Carter", null));
    System.err.println(StringUtil.valueOf(users1, true));
    Assertions.assertEquals(2, users1.size());
    Assertions.assertIterableEquals(users, users1);
  }

  @Test
  public void selectResultOne3() {
    Assertions.assertThrows(IncorrectResultSizeDataAccessException.class,
        () -> repository.selectResultOne3(new User("Carter", null)));
  }

  @Test
  public void selectResultFirst3() {
    AUser users = repository.selectResultFirst3(new User("Carter", null));
    System.err.println(StringUtil.valueOf(users, true));
  }

  @Test
  public void selectResultMap3Page() {
    Page<AUser> users = repository.selectResultMap3(new User("Carter", null),
        PageRequest.of(0, 10));
    System.err.println(StringUtil.valueOf(users, true));
    Assertions.assertEquals(2, users.getTotalElements());
    List<Object> users1 = sqlSession
        .selectList(UserRepository.class.getName() + ".selectResultMap3",
            new User("Carter", null));
    System.err.println(StringUtil.valueOf(users1, true));
    Assertions.assertEquals(2, users1.size());
    Assertions.assertIterableEquals(users, users1);
  }

  @Test
  public void selectResultMap3Page2() {
    Page<AUser> users = repository.selectResultMap3(new User("Carter", null),
        PageRequest.of(0, 2));
    System.err.println(StringUtil.valueOf(users.getContent(), true));
    Assertions.assertEquals(2, users.getTotalElements());
    PageRequest pageable = PageRequest.of(1, 2);
    System.err.println(pageable.getOffset());
    users = repository.selectResultMap3(new User("Carter", null),
        pageable);
    System.err.println(StringUtil.valueOf(users.getContent(), true));
  }

  @Test
  public void selectMybatisAll() {
    List<User> users = repository.selectMybatisAll();
    System.err.println(StringUtil.valueOf(users, true));
    List<Object> users1 = sqlSession
        .selectList(UserRepository.class.getName() + ".selectMybatisAll");
    System.err.println(StringUtil.valueOf(users1, true));
    Assertions.assertIterableEquals(users, users1);
    Assertions.assertEquals(4, users.size());
  }

  @Test
  public void selectMybatisMapList() {
    List<Map<String, String>> users = repository.selectMybatisMapList();
    System.err.println(StringUtil.valueOf(users, true));
    List<Object> users1 = sqlSession
        .selectList(UserRepository.class.getName() + ".selectMybatisMapList");
    System.err.println(StringUtil.valueOf(users1, true));
    Assertions.assertIterableEquals(users, users1);
    Assertions.assertEquals(4, users.size());
  }

  @Test
  public void selectMybatisMap() {
    Map<String, String> users = repository.selectMybatisMap();
    System.err.println(StringUtil.valueOf(users, true));
    Object users1 = sqlSession
        .selectOne(UserRepository.class.getName() + ".selectMybatisMap");
    System.err.println(StringUtil.valueOf(users1, true));
    Assertions.assertNull(users1);
  }

  @Test
  void testService() {
    service.testService();
  }

  @Test
  public void selectMybatisAllPage() {
    Page<User> users = repository.selectMybatisAll(PageRequest.of(0, 1));
    for (User user : users) {
      System.err.println(user);
    }

    System.err.println("===========" + users.getTotalElements());
    System.err.println("===========" + users.getContent().size());
    Assertions.assertEquals(4, users.getTotalElements());
    Assertions.assertEquals(1, users.getContent().size());
  }

  @Test
  public void selectMybatisAllPageAll() {
    Page<User> users = repository.selectMybatisAll(Pageable.unpaged());
    for (User user : users) {
      System.err.println(user);
    }
    System.err.println("===========" + users.getTotalElements());
    System.err.println("===========" + users.getContent().size());
    Assertions.assertEquals(4, users.getTotalElements());
    Assertions.assertEquals(4, users.getContent().size());
  }

  @Test
  public void selectByMybatisSize() {
    List<User> users = repository.selectByMybatisSize(Size.of(2));
    for (User user : users) {
      System.err.println(user);
    }
    System.err.println("===========" + users.size());
    Assertions.assertEquals(2, users.size());
  }

  @Test
  public void selectMybatisIfParam() {
    List<User> users = repository.selectMybatisIfParam("Carter", "Beauford1");
    System.err.println(users);
    List<Object> users1 = sqlSession
        .selectList(UserRepository.class.getName() + ".selectMybatisIfParam",
            CollectionUtil.mapOf("firstName", "Carter", "lastName", "Beauford1",
                "param2", "Beauford1"));
    System.err.println(users1);
    Assertions.assertIterableEquals(users, users1);
    Assertions.assertEquals(1, users.size());
  }

  @Test
  @Transactional(readOnly = true)
  public void selectMybatisStream() {
    try (Stream<User> users = repository.selectMybatisStream("Carter", null)) {
      users.forEach(System.err::println);
    }
  }

  final CountDownLatch countDownLatch = new CountDownLatch(200);

  @Test
  public void selectMybatisIfParamAsynchronous() throws Exception {
    List<Throwable> errors = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      new Thread(() -> {
        try {
          List<User> users = repository.selectMybatisIfParam("Carter", "Beauford1");
          System.err.println(users);
          List<Object> users1 = sqlSession
              .selectList(UserRepository.class.getName() + ".selectMybatisIfParam",
                  CollectionUtil.mapOf("firstName", "Carter",
                      "lastName", "Beauford1",
                      "param2", "Beauford1"));
          System.err.println(users1);
          Assertions.assertIterableEquals(users, users1);
          Assertions.assertEquals(1, users.size());
        } catch (Throwable e) {
          errors.add(e);
        } finally {
          countDownLatch.countDown();
        }
      }).start();
      new Thread(() -> {
        try {
          List<User> users2 = repository.selectMybatisIfParam("Carter", null);
          System.err.println(users2);
          List<Object> users21 = sqlSession
              .selectList(UserRepository.class.getName() + ".selectMybatisIfParam",
                  MapsKt.mapOf(new Pair<>("firstName", "Carter"))
              );
          System.err.println(users21);
          Assertions.assertIterableEquals(users2, users21);
          Assertions.assertEquals(2, users2.size());
        } catch (Throwable e) {
          errors.add(e);
        } finally {
          countDownLatch.countDown();
        }
      }).start();
    }
    countDownLatch.await();
    System.err.println("/////////////////");
    errors.forEach(Throwable::printStackTrace);
    Assertions.assertEquals(0, errors.size());
  }

  @Test
  public void selectByMybatisMap() {
    Map<String, String> params = new HashMap<>();
    params.put("firstName", "Carter");
    params.put("lastName", "Beauford1");
    List<User> users = repository.selectByMybatisMap(params);
    System.err.println(users);
    List<Object> users1 = sqlSession
        .selectList(UserRepository.class.getName() + ".selectByMybatisMap", params);
    System.err.println(users1);
    Assertions.assertIterableEquals(users, users1);
    Assertions.assertEquals(1, users.size());
  }


  @Test
  public void selectByMybatisMapPage() {
    Map<String, String> params = new HashMap<>();
    params.put("firstName", "Carter");
    Page<User> users = repository
        .selectByMybatisMap(PageRequest.of(0, 2, Sort.by(Direction.ASC, "lastName")), params);
    System.err.println(StringUtil.valueOf(users.getContent(), true));
    List<Object> users1 = sqlSession
        .selectList(UserRepository.class.getName() + ".selectByMybatisMap", params);
    System.err.println(StringUtil.valueOf(users1, true));
    Assertions.assertEquals(2, users.getTotalElements());
    List<User> userList = users.getContent();
    Assertions.assertEquals(2, userList.size());
    Assertions.assertEquals(userList.get(0).getLastName(), "Beauford1");
    Assertions.assertEquals("Beauford2", repository
        .selectByMybatisMap(PageRequest.of(0, 2, Sort.by(Direction.DESC, "lastName")), params)
        .getContent().get(0).getLastName());
    List<User> users2 = repository.selectByMybatisMap(params);
    System.err.println(users2);
    Assertions.assertEquals(2, users2.size());
    Assertions.assertEquals("Beauford1", users2.get(0).getLastName());
  }

  @Test
  public void selectByMybatisEntity() {
    List<User> users = repository.selectByMybatisEntity(new User("Carter", null),
        Pageable.unpaged());
    System.err.println(users);
    Assertions.assertEquals(2, users.size());
    List<Object> users1 = sqlSession
        .selectList(UserRepository.class.getName() + ".selectByMybatisEntity",
            new User("Carter", null));
    System.err.println(users1);
    Assertions.assertEquals(2, users1.size());
    Assertions.assertIterableEquals(users, users1);
  }


  @Test
  public void selectByMybatisSort() {
    List<User> users = repository.selectByMybatisSort("Carter", Sort.by(Direction.ASC, "lastName"));
    System.err.println(users);
    Assertions.assertEquals(2, users.size());
    Assertions.assertEquals(users.get(0).getLastName(), "Beauford1");
    users = repository.selectByMybatisSort("Carter", Sort.by(Direction.DESC, "lastName"));
    System.err.println(users);
    Assertions.assertEquals(2, users.size());
    Assertions.assertEquals("Beauford2", users.get(0).getLastName());
  }


  @Test
  public void selectByMybatisSortPage() {
    Page<User> users = repository
        .selectByMybatisSort("Carter", PageRequest.of(1, 1, Sort.by(Direction.DESC, "lastName")));
    System.err.println(users);
    System.err.println(users.getContent());
    Assertions.assertEquals(2, users.getTotalElements());
    Assertions.assertEquals(1, users.getContent().size());
    Assertions
        .assertEquals("Beauford1", users.getContent().get(0).getLastName());
    users = repository
        .selectByMybatisSort("Carter", PageRequest.of(0, 1, Sort.by(Direction.ASC, "lastName")));
    System.err.println(users);
    Assertions.assertEquals(2, users.getTotalElements());
    Assertions.assertEquals(1, users.getContent().size());
    Assertions.assertEquals(users.getContent().get(0).getLastName(), "Beauford1");
    users = repository
        .selectByMybatisSort("Carter", PageRequest.of(0, 1, Sort.by(Direction.DESC, "lastName")));
    System.err.println(users);
    Assertions.assertEquals(2, users.getTotalElements());
    Assertions.assertEquals(1, users.getContent().size());
    Assertions
        .assertEquals("Beauford2", users.getContent().get(0).getLastName());
    users = repository
        .selectByMybatisSort("Carter", PageRequest.of(1, 1, Sort.by(Direction.DESC, "lastName")));
    System.err.println(users.getContent());
    Assertions.assertEquals(2, users.getTotalElements());
    Assertions.assertEquals(1, users.getContent().size());
    Assertions
        .assertEquals("Beauford1", users.getContent().get(0).getLastName());
  }

  @Test
  public void findOneByMybatis() {
    User user = repository.selectOneByMybatis("Dave");
    System.err.println(user);
    Assertions.assertNotNull(user);
    Object user1 = sqlSession
        .selectOne(UserRepository.class.getName() + ".selectOneByMybatis",
            "Dave");
    System.err.println(user1);
    Assertions.assertNotNull(user1);
    Assertions.assertEquals(user, user1);
    user = repository.selectOneByMybatis("Dave2");
    System.err.println(user);
    Assertions.assertNull(user);
    user1 = sqlSession
        .selectOne(UserRepository.class.getName() + ".selectOneByMybatis",
            "Dave2");
    System.err.println(user1);
    Assertions.assertNull(user1);
    Assertions.assertEquals(user, user1);
  }


  @Test
  public void insert() {
    int insert = repository.insert("Wu", "Peter");
    List<User> peter = repository.findByLastName("Peter");
    System.err.println(peter);
    Assertions.assertEquals(1, insert);
    Assertions.assertEquals(1, peter.size());
  }

  @Test
  public void update() {
    int update = repository.update(carterId, "Peter");
    Optional<User> userOptional = repository.findById(carterId);
    Assertions.assertTrue(userOptional.isPresent());
    User peter = userOptional.get();
    System.err.println(peter);
    Assertions.assertEquals(1, update);
    Assertions.assertEquals("Peter", peter.getLastName());
  }

  @Test
  public void updateNoReturn() {
    repository.updateNoReturn(carterId, "Peter");
    Optional<User> userOptional = repository.findById(carterId);
    Assertions.assertTrue(userOptional.isPresent());
    User peter = userOptional.get();
    System.err.println(peter);
    Assertions.assertEquals("Peter", peter.getLastName());
  }

  @Test
  public void deleteMybatis() {
    int delete = repository.deleteMybatis(carterId);
    Optional<User> userOptional = repository.findById(carterId);
    Assertions.assertFalse(userOptional.isPresent());
    Assertions.assertEquals(1, delete);
  }

  @Test
  public void deleteMybatisNoResturn() {
    repository.deleteMybatisNoResturn(carterId);
    Optional<User> userOptional = repository.findById(carterId);
    Assertions.assertFalse(userOptional.isPresent());
  }


}