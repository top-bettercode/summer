package cn.bestwu.simpleframework.data.test.test;

import cn.bestwu.simpleframework.data.test.domain.HardUser;
import cn.bestwu.simpleframework.data.test.domain.QUser;
import cn.bestwu.simpleframework.data.test.domain.User;
import cn.bestwu.simpleframework.data.test.repository.HardUserRepository;
import cn.bestwu.simpleframework.data.test.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SoftDeleteTest {

  @Autowired
  UserRepository repository;
  @Autowired
  HardUserRepository hardUserRepository;
  @Autowired
  DataSource dataSource;
  final List<User> batch = new ArrayList<>();
  final List<Integer> batchIds = new ArrayList<>();
  Integer daveId;
  Integer carterId;

  @BeforeEach
  public void setUp() {
//    RunScript.execute(dataSource.getConnection(),
//        new FileReader(new ClassPathResource("import.sql").getFile()));

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

  @Test
  public void name() {
    System.err.println(repository.findAll(QUser.user.firstname.contains("D")));
  }

  @AfterEach
  public void tearDown() {
    repository.deleteAll();
    repository.cleanRecycleBin();
    hardUserRepository.deleteAll();
    hardUserRepository.cleanRecycleBin();
  }

  @Test
  public void hardDeleteTest() {
    HardUser dave = new HardUser("Dave", "Matthews");
    hardUserRepository.save(dave);
    hardUserRepository.delete(dave);
    Optional<HardUser> optionalUser = hardUserRepository.findByIdFromRecycleBin(dave.getId());
    optionalUser.ifPresent(System.out::println);
    org.junit.jupiter.api.Assertions.assertFalse(optionalUser.isPresent());
  }

  @Test
  public void methdQuery() {
    repository.deleteAllInBatch(batch);

    List<User> users = repository.findByLastname("Matthews");
    System.err.println(users);
    org.junit.jupiter.api.Assertions.assertEquals(0, users.size());

    users = repository.findByLastname("Beauford");
    System.err.println(users);
    org.junit.jupiter.api.Assertions.assertEquals(2, users.size());

    List<User> recycleAll = repository.findAllFromRecycleBin();
    System.err.println(recycleAll);
    org.junit.jupiter.api.Assertions.assertEquals(2, recycleAll.size());

  }

  @Test
  public void methdDelete() {
    repository.deleteByLastname("Matthews");

    List<User> users = repository.findByLastname("Matthews");
    System.err.println(users);
    org.junit.jupiter.api.Assertions.assertEquals(0, users.size());

    users = repository.findByLastname("Beauford");
    System.err.println(users);
    org.junit.jupiter.api.Assertions.assertEquals(2, users.size());

    List<User> recycleAll = repository.findAllFromRecycleBin();
    System.err.println(recycleAll);
    org.junit.jupiter.api.Assertions.assertEquals(2, recycleAll.size());

  }
}