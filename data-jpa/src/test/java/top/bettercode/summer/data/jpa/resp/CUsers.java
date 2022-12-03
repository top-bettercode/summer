package top.bettercode.summer.data.jpa.resp;

import java.util.List;
import java.util.Objects;
import top.bettercode.summer.tools.lang.util.StringUtil;

/**
 * @author Peter Wu
 */
public class CUsers {

  private String firstName;
  private List<LastName> lastName;

  public CUsers() {
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }


  public List<LastName> getLastName() {
    return lastName;
  }

  public void setLastName(
      List<LastName> lastName) {
    this.lastName = lastName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CUsers)) {
      return false;
    }
    CUsers cUsers = (CUsers) o;
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
