package top.bettercode.simpleframework.data.jpa.query.mybatis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.ibatis.mapping.MappedStatement;
import org.hibernate.jpa.spi.NativeQueryTupleTransformer;

/**
 * @author Peter Wu
 */
public class MybatisResultTransformer extends NativeQueryTupleTransformer {

  private static final long serialVersionUID = 1L;

  private final MappedStatement mappedStatement;

  public MybatisResultTransformer(MappedStatement mappedStatement) {
    this.mappedStatement = mappedStatement;
  }


  public Object transform(ResultSet resultSet) throws SQLException {
    return this.transformList(resultSet, 1).get(0);
  }

  public List<?> transformList(ResultSet resultSet, int maxRows) throws SQLException {
    return new MybatisResultSetHandler(mappedStatement).handleResultSets(resultSet, maxRows);
  }
}
