package top.bettercode.simpleframework.data.test.domain;

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
  private String firstname;
  private String lastname;
  private boolean deleted;

  public HardUser() {
  }

  public HardUser(String firstname, String lastname) {
    this.firstname = firstname;
    this.lastname = lastname;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  @Override
  public String toString() {
    return "BaseUser{" +
        "id=" + id +
        ", firstname='" + firstname + '\'' +
        ", lastname='" + lastname + '\'' +
        ", deleted=" + deleted +
        '}';
  }
}