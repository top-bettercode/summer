package top.bettercode.simpleframework.data.test.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import org.hibernate.annotations.Type;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.data.jpa.SoftDelete;

@MappedSuperclass
public class BaseUser {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;
  @Column(name = "first_name")
  private String firstName;
  @Column(name = "last_name")
  private String lastName;

  @Column(name = "deleted", columnDefinition = "TINYINT(1) DEFAULT 0", length = 1, nullable = false)
  @SoftDelete
  @Type(type = "org.hibernate.type.NumericBooleanType")
  private Boolean deleted;

  public BaseUser() {
  }

  public BaseUser(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.deleted = false;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BaseUser)) {
      return false;
    }
    BaseUser baseUser = (BaseUser) o;
    return deleted == baseUser.deleted && Objects.equals(id, baseUser.id)
        && Objects.equals(firstName, baseUser.firstName) && Objects.equals(
        lastName, baseUser.lastName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, firstName, lastName, deleted);
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}