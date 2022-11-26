package top.bettercode.simpleframework.data.test.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.hibernate.annotations.ColumnDefault;
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
  private String name;
  @SoftDelete
  @Type(type = "org.hibernate.type.NumericBooleanType")
  @ColumnDefault("0")
  private Boolean deleted;

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

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}