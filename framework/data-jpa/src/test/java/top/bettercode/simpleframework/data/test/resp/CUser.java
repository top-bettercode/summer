package top.bettercode.simpleframework.data.test.resp;

import java.util.List;

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
}
