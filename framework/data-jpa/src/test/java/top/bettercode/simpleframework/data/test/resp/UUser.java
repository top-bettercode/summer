package top.bettercode.simpleframework.data.test.resp;

/**
 * @author Peter Wu
 */
public class UUser  {

  private String firstName;
  private String lastName;

  public UUser() {
  }

  public UUser(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
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
}
