package cn.bestwu.simpleframework.data.test.test;

import cn.bestwu.simpleframework.data.test.domain.User;
import cn.bestwu.simpleframework.data.test.repository.UserDao;
import cn.bestwu.simpleframework.data.test.repository.UserRepository;
import com.github.pagehelper.PageHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MybatisTest {

  @Autowired
  UserRepository repository;
  @Autowired
  UserDao userDao;
  @Autowired
  SqlSession sqlSession;
  Integer carterId;

  @Before
  public void setUp() {
    User dave = new User("Dave", "Matthews");
    repository.save(dave);
    User dave1 = new User("Dave", "Matthews");
    repository.save(dave1);
    User carter = new User("Carter", "Beauford1");
    repository.save(carter);
    carter = new User("Carter", "Beauford2");
    repository.save(carter);
    carterId = carter.getId();
    repository.delete(dave);
  }

  @After
  public void tearDown() {
    repository.deleteAll();
    repository.cleanRecycleBin();
  }

  @Test
  public void findByMybatis() {
    List<User> users = repository.findByMybatis();
    System.err.println(users);
    Assert.assertEquals(3, users.size());
  }

  @Test
  public void insert() {
    int insert = repository.insert("Wu", "Peter");
    List<User> peter = repository.findByLastname("Peter");
    System.err.println(peter);
    Assert.assertEquals(1, insert);
    Assert.assertEquals(1, peter.size());
  }

  @Test
  public void update() {
    int update = repository.update(carterId, "Peter");
    Optional<User> userOptional = repository.findById(carterId);
    Assert.assertTrue(userOptional.isPresent());
    User peter = userOptional.get();
    System.err.println(peter);
    Assert.assertEquals(1, update);
    Assert.assertEquals("Peter", peter.getLastname());
  }

  @Test
  public void deleteBy() {
    int delete = repository.deleteBy(carterId);
    Optional<User> userOptional = repository.findById(carterId);
    Assert.assertFalse(userOptional.isPresent());
    Assert.assertEquals(1, delete);
  }

  @Test
  public void findByMybatis2() {
    List<User> users = repository.findByMybatis2("Carter", "Beauford1");
    System.err.println(users);
    Assert.assertEquals(1, users.size());
  }

  @Test
  public void findByMybatis222() {
    Map<String, String> params = new HashMap<>();
    params.put("firstname", "Carter");
    params.put("param2", "Beauford1");
    List<User> users = repository.findByMybatis2(params);
    System.err.println(users);
    Assert.assertEquals(1, users.size());
  }

  @Test
  public void findByMybatis2222() {
    Map<String, String> params = new HashMap<>();
    params.put("firstname", "Carter");
    Page<User> users = repository
        .findByMybatis2(PageRequest.of(0, 1, Sort.by(Direction.ASC, "lastname")), params);
    System.err.println(users);
    Assert.assertEquals(2, users.getTotalElements());
    List<User> userList = users.getContent();
    Assert.assertEquals(1, userList.size());
    Assert.assertEquals("Beauford1", userList.get(0).getLastname());
    Assert.assertEquals("Beauford2", repository
        .findByMybatis2(PageRequest.of(0, 1, Sort.by(Direction.DESC, "lastname")), params)
        .getContent().get(0).getLastname());
    List<User> users2 = repository.findByMybatis2(params);
    System.err.println(users2);
    Assert.assertEquals(2, users2.size());
    Assert.assertEquals("Beauford1", users2.get(0).getLastname());
  }

  @Test
  public void findByMybatis22() {
    List<User> users = repository.findByMybatis22(new User("Carter", "Beauford1"));
    System.err.println(users);
    Assert.assertEquals(1, users.size());
  }


  @Test
  public void findByMybatis2212() {
    User user = new User("Carter", null);
    Page<User> users = repository
        .findByMybatis22(user, PageRequest.of(0, 1, Sort.by(Direction.ASC, "lastname")));
    System.err.println(users);
    Assert.assertEquals(2, users.getTotalElements());
    Assert.assertEquals(1, users.getContent().size());
    Assert.assertEquals("Beauford1", users.getContent().get(0).getLastname());
    users = repository
        .findByMybatis22(user, PageRequest.of(0, 1, Sort.by(Direction.DESC, "lastname")));
    System.err.println(users);
    Assert.assertEquals(2, users.getTotalElements());
    Assert.assertEquals(1, users.getContent().size());
    Assert.assertEquals("Beauford2", users.getContent().get(0).getLastname());
  }

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Test
  public void findByMybatis22122() {
    expectedException.expect(InvalidDataAccessApiUsageException.class);
    expectedException.expectMessage(
        "当包含org.springframework.data.domain.Pageable参数时返回类型必须为org.springframework.data.domain.Page");
    User user = new User("Carter", null);
    repository
        .findByMybatis222(user, PageRequest.of(0, 1, Sort.by(Direction.ASC, "lastname")));
  }

  @Test
  public void findOneByMybatis() {
    User user = repository.findOneByMybatis("Dave");
    System.err.println(user);
    Assert.assertNotNull(user);
    user = repository.findOneByMybatis("Dave2");
    System.err.println(user);
    Assert.assertNull(user);
  }

  @Test
  public void findByMybatis3() {
    List<User> users = repository.findByMybatis3("Carter", Sort.by(Direction.ASC, "lastname"));
    System.err.println(users);
    Assert.assertEquals(2, users.size());
    Assert.assertEquals("Beauford1", users.get(0).getLastname());
    users = repository.findByMybatis3("Carter", Sort.by(Direction.DESC, "lastname"));
    System.err.println(users);
    Assert.assertEquals(2, users.size());
    Assert.assertEquals("Beauford2", users.get(0).getLastname());
  }

  @Test
  public void findByMybatis30() {
    Page<User> users = repository
        .findByFirstname("Carter", PageRequest.of(0, 1, Sort.by(Direction.ASC, "lastname")));
    System.err.println(users);
    Assert.assertEquals(2, users.getTotalElements());
    Assert.assertEquals(1, users.getContent().size());
    Assert.assertEquals("Beauford1", users.getContent().get(0).getLastname());
    users = repository
        .findByFirstname("Carter", PageRequest.of(0, 1, Sort.by(Direction.DESC, "lastname")));
    System.err.println(users);
    Assert.assertEquals(2, users.getTotalElements());
    Assert.assertEquals(1, users.getContent().size());
    Assert.assertEquals("Beauford2", users.getContent().get(0).getLastname());
    users = repository
        .findByFirstname("Carter", PageRequest.of(1, 1, Sort.by(Direction.DESC, "lastname")));
    System.err.println(users);
    Assert.assertEquals(2, users.getTotalElements());
    Assert.assertEquals(1, users.getContent().size());
    Assert.assertEquals("Beauford1", users.getContent().get(0).getLastname());
  }

  @Test
  public void findByMybatis31() {
    Page<User> users = repository
        .findByMybatis3("Carter", PageRequest.of(0, 1, Sort.by(Direction.ASC, "lastname")));
    System.err.println(users);
    Assert.assertEquals(2, users.getTotalElements());
    Assert.assertEquals(1, users.getContent().size());
    Assert.assertEquals("Beauford1", users.getContent().get(0).getLastname());
    users = repository
        .findByMybatis3("Carter", PageRequest.of(0, 1, Sort.by(Direction.DESC, "lastname")));
    System.err.println(users);
    Assert.assertEquals(2, users.getTotalElements());
    Assert.assertEquals(1, users.getContent().size());
    Assert.assertEquals("Beauford2", users.getContent().get(0).getLastname());
    users = repository
        .findByMybatis3("Carter", PageRequest.of(1, 1, Sort.by(Direction.DESC, "lastname")));
    System.err.println(users);
    Assert.assertEquals(2, users.getTotalElements());
    Assert.assertEquals(1, users.getContent().size());
    Assert.assertEquals("Beauford1", users.getContent().get(0).getLastname());
  }

  @Test
  public void findByMybatis2233() {
    com.github.pagehelper.Page<Object> page = PageHelper.startPage(2, 2).doSelectPage(
        () -> sqlSession
            .selectList(UserRepository.class.getName() + ".findByMybatis"));
    System.err.println(page.getResult());
    System.err.println(page.getTotal());
  }

  @Test
  public void findByMybatisByUserDao() {
    System.err.println(userDao.findByMybatis());
  }
}