package top.bettercode.summer.data.jpa.test;

import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import top.bettercode.summer.data.jpa.domain.StaticUser;
import top.bettercode.summer.data.jpa.domain.User;
import top.bettercode.summer.data.jpa.repository.StaticUserRepository;
import top.bettercode.summer.data.jpa.repository.UserRepository;

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
    System.err.println("--------------------------------------------------------");
  }


  @AfterEach
  public void tearDown() {
    System.err.println("--------------------------------------------------------");
  }

  @Deprecated
  @Test
  public void dynamicSaveTest() {
    User dave = new User("Wu", "Matthews");
    dave = repository.save(dave);

    Integer id = dave.getId();
    Optional<User> optionalUser = repository.findById(id);
    org.junit.jupiter.api.Assertions.assertTrue(optionalUser.isPresent());
    optionalUser.ifPresent(user -> {
          System.err.println(user);
          Assertions.assertNotNull(user.getFirstName());
        }
    );
    dave = new User();
    dave.setId(id);
    dave.setLastName("MM");
    repository.dynamicSave(dave);
    optionalUser = repository.findById(id);
    org.junit.jupiter.api.Assertions.assertTrue(optionalUser.isPresent());
    optionalUser.ifPresent(user -> {
          System.err.println(user);
          Assertions.assertNotNull(user.getFirstName());
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
          org.junit.jupiter.api.Assertions.assertNull(user.getFirstName());
        }
    );
  }
}