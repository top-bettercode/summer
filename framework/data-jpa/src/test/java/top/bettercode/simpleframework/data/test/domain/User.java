package top.bettercode.simpleframework.data.test.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Version;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@DynamicUpdate
@Entity
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseUser{

  @Version
  @Column(name = "version", columnDefinition = "INT(11) DEFAULT 0", length = 11)
  private Integer version;

  public User() {
  }

  public User(String firstName, String lastName) {
    super(firstName, lastName);
  }


  public Integer getVersion() {
    return version;
  }

  public User setVersion(Integer version) {
    this.version = version;
    return this;
  }
}