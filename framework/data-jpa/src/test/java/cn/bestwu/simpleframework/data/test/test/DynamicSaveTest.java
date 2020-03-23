package cn.bestwu.simpleframework.data.test.test;

import cn.bestwu.simpleframework.data.test.domain.StaticUser;
import cn.bestwu.simpleframework.data.test.domain.User;
import cn.bestwu.simpleframework.data.test.repository.StaticUserRepository;
import cn.bestwu.simpleframework.data.test.repository.UserRepository;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DynamicSaveTest {

  @Autowired
  UserRepository repository;
  @Autowired
  StaticUserRepository staticUserRepository;
  @Autowired
  DataSource dataSource;

  @Before
  public void setUp() throws Exception {
//    RunScript.execute(dataSource.getConnection(),
//        new FileReader(new ClassPathResource("data.sql").getFile()));
  }

  @Test
  public void name() {

  }

  @Test
  public void dynamicSaveTest() {
    User dave = new User(null, "Matthews");
    dave = repository.save(dave);

    Optional<User> optionalUser = repository.findById(dave.getId());
    Assert.assertTrue(optionalUser.isPresent());
    optionalUser.ifPresent(user -> {
          System.err.println(user);
          Assert.assertEquals("wu", user.getFirstname());
        }
    );
    dave.setLastname("MM");
    repository.dynamicSave(dave);
    optionalUser = repository.findById(dave.getId());
    Assert.assertTrue(optionalUser.isPresent());
    optionalUser.ifPresent(user -> {
          System.err.println(user);
          Assert.assertEquals("wu", user.getFirstname());
        }
    );
  }

  @Test
  public void staticSaveTest() {
    StaticUser dave = new StaticUser(null, "Matthews");
    dave = staticUserRepository.save(dave);

    Optional<StaticUser> optionalUser = staticUserRepository.findById(dave.getId());
    Assert.assertTrue(optionalUser.isPresent());
    optionalUser.ifPresent(user -> {
          System.err.println(user);
          Assert.assertNull(user.getFirstname());
        }
    );
  }
}