package top.bettercode.simpleframework.data.test.domain;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import top.bettercode.lang.util.StringUtil;

@Entity
@Table(name = "t_user")
public class HardUser {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;
  private String firstName;
  private String lastName;
  @Type(type = "org.hibernate.type.NumericBooleanType")
  @ColumnDefault("0")
  private Boolean deleted;

  public HardUser() {
  }

  public HardUser(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
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
    if (!(o instanceof HardUser)) {
      return false;
    }
    HardUser hardUser = (HardUser) o;
    return deleted == hardUser.deleted && Objects.equals(id, hardUser.id)
        && Objects.equals(firstName, hardUser.firstName) && Objects.equals(
        lastName, hardUser.lastName);
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