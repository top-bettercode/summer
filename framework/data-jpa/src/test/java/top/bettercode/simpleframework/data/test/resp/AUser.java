package top.bettercode.simpleframework.data.test.resp;

import java.util.Objects;
import top.bettercode.lang.util.StringUtil;

/**
 * @author Peter Wu
 */
public class AUser {

  private Integer id;
  private String firstName;
  private LastName lastName;

  public AUser() {
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


  public LastName getLastName() {
    return lastName;
  }

  public void setLastName(LastName lastName) {
    this.lastName = lastName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AUser)) {
      return false;
    }
    AUser cUsers = (AUser) o;
    return Objects.equals(firstName, cUsers.firstName) && Objects.equals(lastName,
        cUsers.lastName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstName, lastName);
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}
