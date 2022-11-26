package top.bettercode.simpleframework.data.test.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_user")
public class StaticUser extends BaseUser {

  public StaticUser() {
  }

  public StaticUser(String firstName, String lastName) {
    super(firstName, lastName);
  }

}