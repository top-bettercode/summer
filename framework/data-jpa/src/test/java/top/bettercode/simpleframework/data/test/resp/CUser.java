package top.bettercode.simpleframework.data.test.resp;

import java.util.List;
import java.util.Objects;
import top.bettercode.lang.util.StringUtil;

/**
 * @author Peter Wu
 */
public class CUser {

  private String firstName;
  private List<String> lastName;

  public CUser() {
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public List<String> getLastName() {
    return lastName;
  }

  public void setLastName(List<String> lastName) {
    this.lastName = lastName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CUser)) {
      return false;
    }
    CUser cUser = (CUser) o;
    return Objects.equals(firstName, cUser.firstName) && Objects.equals(lastName,
        cUser.lastName);
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
