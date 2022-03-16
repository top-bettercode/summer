package top.bettercode.simpleframework.data.test.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "User")
public class HardUser {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;
  @Column(name = "first_name")
  private String firstName;
  @Column(name = "last_name")
  private String lastName;
  private boolean deleted;

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

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
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
    return "BaseUser{" +
        "id=" + id +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", deleted=" + deleted +
        '}';
  }
}