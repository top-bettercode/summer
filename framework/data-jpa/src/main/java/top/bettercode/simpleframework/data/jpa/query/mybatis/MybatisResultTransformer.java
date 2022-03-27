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

  public MybatisResultSetHandler getMybatisResultSetHandler() {
    return mybatisResultSetHandler;
  }

  public Object transform(ResultSet resultSet) throws SQLException {
    return this.transformList(resultSet).get(0);
  }

  public List<?> transformList(ResultSet resultSet) throws SQLException {
    Statement statement = resultSet.getStatement();
    return mybatisResultSetHandler.handleResultSets(statement);
  }
}
