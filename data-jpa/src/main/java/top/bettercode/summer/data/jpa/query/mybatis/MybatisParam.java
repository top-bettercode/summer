package top.bettercode.summer.data.jpa.query.mybatis;

import org.apache.ibatis.mapping.BoundSql;
import top.bettercode.summer.data.jpa.support.Size;

/**
 * @author Peter Wu
 */
public class MybatisParam {

  private final BoundSql boundSql;
  private final Object parameterObject;
  private final Size size;

  public MybatisParam(BoundSql boundSql, Object parameterObject,
      Size size) {
    this.boundSql = boundSql;
    this.parameterObject = parameterObject;
    this.size = size;
  }

  public BoundSql getBoundSql() {
    return boundSql;
  }

  public Object getParameterObject() {
    return parameterObject;
  }

  public Size getSize() {
    return size;
  }
}
