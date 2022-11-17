package top.bettercode.simpleframework.data.test.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.data.jpa.SoftDelete;

@DynamicUpdate
@Entity
public class Job {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "jobid")
  @GenericGenerator(name = "jobid", strategy = "uuid2")
  private String id;
  @Column(name = "name")
  private String name;
  @SoftDelete
  @Type(type = "org.hibernate.type.NumericBooleanType")
  private boolean deleted;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}