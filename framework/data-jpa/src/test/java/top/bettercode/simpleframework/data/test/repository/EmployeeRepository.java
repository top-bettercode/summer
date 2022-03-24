package top.bettercode.simpleframework.data.test.repository;

import top.bettercode.simpleframework.data.jpa.JpaExtRepository;
import top.bettercode.simpleframework.data.test.domain.Employee;
import top.bettercode.simpleframework.data.test.domain.EmployeeKey;

/**
 * @author Peter Wu
 */
public interface EmployeeRepository extends JpaExtRepository<Employee, EmployeeKey> {

}
