package top.bettercode.summer.data.jpa.domain;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;
import top.bettercode.summer.tools.lang.util.StringUtil;

/**
 * 客商档案 主键 对应表名：BD_CVDOC
 */
@Embeddable
public class EmployeeKey implements Serializable {

  private static final long serialVersionUID = 1L;

  private Integer id;
  private Integer id2;

  public EmployeeKey() {
  }

  public EmployeeKey(Integer id, Integer id2) {
    this.id = id;
    this.id2 = id2;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getId2() {
    return id2;
  }

  public void setId2(Integer id2) {
    this.id2 = id2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EmployeeKey)) {
      return false;
    }
    EmployeeKey that = (EmployeeKey) o;
    return Objects.equals(id, that.id) && Objects.equals(id2, that.id2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, id2);
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}