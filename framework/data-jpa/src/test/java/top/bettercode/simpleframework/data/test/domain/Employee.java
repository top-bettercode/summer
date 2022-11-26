package top.bettercode.simpleframework.data.test.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.data.jpa.SoftDelete;

@DynamicUpdate
@Entity
public class Employee {

  @EmbeddedId
  private EmployeeKey employeeKey;
  private String firstName;
  private String lastName;
  @SoftDelete
  @Type(type = "org.hibernate.type.NumericBooleanType")
  @ColumnDefault("0")
  private Boolean deleted;

  public Employee() {
  }

  public Employee(EmployeeKey employeeKey, String firstName, String lastName) {
    this.employeeKey = employeeKey;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public EmployeeKey getEmployeeKey() {
    return employeeKey;
  }

  public void setEmployeeKey(EmployeeKey employeeKey) {
    this.employeeKey = employeeKey;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}