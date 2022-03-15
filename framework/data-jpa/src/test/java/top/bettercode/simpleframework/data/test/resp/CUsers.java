package top.bettercode.simpleframework.data.test.resp;

import java.util.List;

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
}
