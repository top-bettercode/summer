package top.bettercode.simpleframework.data.test.test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import top.bettercode.simpleframework.data.jpa.query.DefaultSpecMatcher;
import top.bettercode.simpleframework.data.test.domain.Employee;
import top.bettercode.simpleframework.data.test.domain.EmployeeKey;
import top.bettercode.simpleframework.data.test.domain.User;
import top.bettercode.simpleframework.data.test.repository.EmployeeRepository;
import top.bettercode.simpleframework.data.test.repository.UserRepository;

/**
 * @author Peter Wu
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class EmployeeRepositoryTest {

  @Autowired
  EntityManager entityManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  EmployeeRepository employeeRepository;

  int delUserId;
  int userId;
  EmployeeKey key;

  @BeforeEach
  public void setUp() {
    User dave = new User("Dave", "Matthews1");
    userRepository.save(dave);
    delUserId = dave.getId();
    User dave1 = new User("Dave", "Matthews2");
    userRepository.save(dave1);
    userId = dave1.getId();
    User carter = new User("Carter", "Beauford1");
    userRepository.save(carter);
    carter = new User("Carter", "Beauford2");
    userRepository.save(carter);

    userRepository.delete(dave);

    Employee employee = new Employee(new EmployeeKey(1, 1), "Dave", "Matthews1");
    employeeRepository.save(employee);
    employeeRepository.delete(employee);
    employee = new Employee(new EmployeeKey(2, 2), "Dave", "Matthews2");
    employeeRepository.save(employee);
    key = employee.getEmployeeKey();
    Employee employee1 = new Employee(new EmployeeKey(1, 1), "Carter", "Beauford1");
    employeeRepository.save(employee1);
    employee1 = new Employee(new EmployeeKey(2, 2), "Carter", "Beauford2");
    employeeRepository.save(employee1);

    System.err.println("--------------------------------------------------------");
  }

  @AfterEach
  public void tearDown() {
    System.err.println("--------------------------------------------------------");
    userRepository.deleteAll();
    userRepository.cleanRecycleBin();
    employeeRepository.deleteAll();
    employeeRepository.cleanRecycleBin();
  }

  @Test
  void delete() {
    DefaultSpecMatcher<User> spec = DefaultSpecMatcher.<User>matching().equal("firstName", "Dave");
    userRepository.delete(spec);
    List<User> all = userRepository.findAll(spec);
    Assertions.assertEquals(0, all.size());
    List<User> allFromRecycleBin = userRepository.findAllFromRecycleBin(spec);
    Assertions.assertEquals(2, allFromRecycleBin.size());
  }


  @Test
  void deleteAllById() {
    userRepository.deleteAllById(Collections.singleton(userId));
    Optional<User> byId = userRepository.findById(userId);
    Assertions.assertFalse(byId.isPresent());
    List<User> allFromRecycleBin = userRepository.findAllByIdFromRecycleBin(
        Collections.singleton(userId));
    Assertions.assertEquals(1, allFromRecycleBin.size());
  }

  @Test
  void deleteAllById2() {
    employeeRepository.deleteAllById(Collections.singleton(key));
    Optional<Employee> byId = employeeRepository.findById(key);
    Assertions.assertFalse(byId.isPresent());
    List<Employee> allFromRecycleBin = employeeRepository.findAllByIdFromRecycleBin(
        Collections.singleton(key));
    Assertions.assertEquals(1, allFromRecycleBin.size());
  }


  @Test
  void deleteAllByIdFromRecycleBin() {
    userRepository.deleteAllByIdFromRecycleBin(Collections.singleton(delUserId));
    Optional<User> byId = userRepository.findByIdFromRecycleBin(delUserId);
    Assertions.assertFalse(byId.isPresent());
  }

  @Test
  void deleteAllByIdFromRecycleBin2() {
    employeeRepository.deleteAllById(Collections.singleton(key));
    employeeRepository.deleteAllByIdFromRecycleBin(Collections.singleton(key));
    Optional<Employee> byId = employeeRepository.findByIdFromRecycleBin(key);
    Assertions.assertFalse(byId.isPresent());
  }

}
