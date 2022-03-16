package top.bettercode.simpleframework.data.test.resp;

import java.util.Objects;

public class LastName {

  private String lastName;
  private boolean deleted;

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
    if (!(o instanceof LastName)) {
      return false;
    }
    LastName lastName1 = (LastName) o;
    return deleted == lastName1.deleted && Objects.equals(lastName, lastName1.lastName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lastName, deleted);
  }
}
