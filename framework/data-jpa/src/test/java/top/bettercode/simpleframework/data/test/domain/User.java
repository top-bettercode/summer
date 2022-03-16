package top.bettercode.simpleframework.data.test.domain;

import javax.persistence.Entity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

@SelectBeforeUpdate
@DynamicInsert
@DynamicUpdate
@Entity
public class User extends BaseUser{

  public User() {
  }

  public User(String firstName, String lastName) {
    super(firstName, lastName);
  }

}