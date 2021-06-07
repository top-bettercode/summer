package top.bettercode.simpleframework.data.test.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "User")
public class StaticUser extends BaseUser{

  public StaticUser() {
  }

  public StaticUser(String firstname, String lastname) {
    super(firstname, lastname);
  }

}