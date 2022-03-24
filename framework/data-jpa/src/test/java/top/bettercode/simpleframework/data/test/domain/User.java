package top.bettercode.simpleframework.data.test.domain;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@DynamicUpdate
@Entity
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseUser{

  public User() {
  }

  public User(String firstName, String lastName) {
    super(firstName, lastName);
  }

}