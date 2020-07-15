package cn.bestwu.simpleframework.data.test.test;

import cn.bestwu.simpleframework.data.test.domain.StaticUser;
import cn.bestwu.simpleframework.data.test.domain.User;
import cn.bestwu.simpleframework.data.test.repository.StaticUserRepository;
import cn.bestwu.simpleframework.data.test.repository.UserRepository;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DynamicSaveTest {

  @Autowired
  UserRepository repository;
  @Autowired
  StaticUserRepository staticUserRepository;
  @Autowired
  DataSource dataSource;

  @BeforeEach
  public void setUp() {
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
    org.junit.jupiter.api.Assertions.assertTrue(optionalUser.isPresent());
    optionalUser.ifPresent(user -> {
          System.err.println(user);
          org.junit.jupiter.api.Assertions.assertEquals("wu", user.getFirstname());
        }
    );
    dave.setLastname("MM");
    repository.dynamicSave(dave);
    optionalUser = repository.findById(dave.getId());
    org.junit.jupiter.api.Assertions.assertTrue(optionalUser.isPresent());
    optionalUser.ifPresent(user -> {
          System.err.println(user);
          org.junit.jupiter.api.Assertions.assertEquals("wu", user.getFirstname());
        }
    );
  }

  @Test
  public void staticSaveTest() {
    StaticUser dave = new StaticUser(null, "Matthews");
    dave = staticUserRepository.save(dave);

    Optional<StaticUser> optionalUser = staticUserRepository.findById(dave.getId());
    org.junit.jupiter.api.Assertions.assertTrue(optionalUser.isPresent());
    optionalUser.ifPresent(user -> {
          System.err.println(user);
          org.junit.jupiter.api.Assertions.assertNull(user.getFirstname());
        }
    );
  }
}