package top.bettercode.summer.data.jpa.query.mybatis;

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


  public Object transform(ResultSet resultSet) {
    return this.transformList(resultSet, 1).get(0);
  }

  public List<?> transformList(ResultSet resultSet) {
    try {
      return transformList(resultSet, resultSet.getFetchSize());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public List<?> transformList(ResultSet resultSet, int maxRows) {
    try {
      if (maxRows == 0) {
        maxRows = Integer.MAX_VALUE;
      }
      return new MybatisResultSetHandler(mappedStatement).handleResultSets(resultSet, maxRows);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
