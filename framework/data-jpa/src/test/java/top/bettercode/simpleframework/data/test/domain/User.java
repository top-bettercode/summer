package top.bettercode.simpleframework.data.test.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Version;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@DynamicUpdate
@Entity
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseUser {

  /**
   * 修改时间 默认值：CURRENT_TIMESTAMP
   */
  @LastModifiedDate
  private LocalDateTime lastModifiedDate;

  /**
   * 创建时间 默认值：CURRENT_TIMESTAMP
   */
  @CreatedDate
  private LocalDateTime createdDate;

  @Version
  @Column(name = "version", columnDefinition = "INT(11) DEFAULT 0", length = 11)
  private Integer version;

  public User() {
  }

  public User(String firstName, String lastName) {
    super(firstName, lastName);
  }

  public LocalDateTime getLastModifiedDate() {
    return lastModifiedDate;
  }

  public User setLastModifiedDate(LocalDateTime lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
    return this;
  }

  public LocalDateTime getCreatedDate() {
    return createdDate;
  }

  public User setCreatedDate(LocalDateTime createdDate) {
    this.createdDate = createdDate;
    return this;
  }

  public Integer getVersion() {
    return version;
  }

  public User setVersion(Integer version) {
    this.version = version;
    return this;
  }


}