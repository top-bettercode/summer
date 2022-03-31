package top.bettercode.simpleframework.data.jpa.query.mybatis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.hibernate.jpa.spi.NativeQueryTupleTransformer;

/**
 * @author Peter Wu
 */
public class MybatisResultTransformer extends NativeQueryTupleTransformer {

  private static final long serialVersionUID = 1L;

  private final MybatisResultSetHandler mybatisResultSetHandler;

  public MybatisResultTransformer(
      MybatisResultSetHandler mybatisResultSetHandler) {
    this.mybatisResultSetHandler = mybatisResultSetHandler;
  }

  public Object transform(ResultSet resultSet) throws SQLException {
    return this.transformList(resultSet, 1).get(0);
  }

  public List<?> transformList(ResultSet resultSet, int maxRows) throws SQLException {
    return mybatisResultSetHandler.handleResultSets(resultSet, maxRows);
  }
}
