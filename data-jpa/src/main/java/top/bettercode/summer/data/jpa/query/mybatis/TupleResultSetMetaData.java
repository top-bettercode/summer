package top.bettercode.summer.data.jpa.query.mybatis;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import org.apache.ibatis.type.JdbcType;
import org.hibernate.type.descriptor.sql.JdbcTypeJavaClassMappings;

/**
 * @author Peter Wu
 */
public class TupleResultSetMetaData implements ResultSetMetaData {

  private final List<String> columnNames = new ArrayList<>();
  private final List<String> classNames = new ArrayList<>();
  private final List<JdbcType> jdbcTypes = new ArrayList<>();

  public TupleResultSetMetaData(List<Tuple> tuples) {
    for (Tuple tuple : tuples) {
      List<TupleElement<?>> elements = tuple.getElements();
      for (int i = 0; i < elements.size(); i++) {
        TupleElement<?> element = elements.get(i);
        String alias = element.getAlias();
        columnNames.add(i, alias);
        Class<?> javaType = element.getJavaType();
        if (!javaType.equals(Object.class)) {
          classNames.add(i, javaType.getName());
          int type = JdbcTypeJavaClassMappings.INSTANCE.determineJdbcTypeCodeForJavaClass(javaType);
          JdbcType jdbcType = JdbcType.forCode(type);
          jdbcTypes.add(i, jdbcType);
        } else if (classNames.size() <= i) {
          classNames.add(i, javaType.getName());
          jdbcTypes.add(i, JdbcType.VARCHAR);
        }
      }
    }
  }

  public int findColumn(String columnName) {
    return columnNames.indexOf(columnName);
  }

  @Override
  public int getColumnCount() throws SQLException {
    return columnNames.size();
  }

  @Override
  public boolean isAutoIncrement(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isCaseSensitive(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isSearchable(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isCurrency(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int isNullable(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isSigned(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getColumnDisplaySize(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public String getColumnLabel(int i) throws SQLException {
    return columnNames.get(i - 1);
  }

  @Override
  public String getColumnName(int i) throws SQLException {
    return columnNames.get(i - 1);
  }

  @Override
  public String getSchemaName(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getPrecision(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getScale(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public String getTableName(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public String getCatalogName(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getColumnType(int i) throws SQLException {
    return jdbcTypes.get(i - 1).TYPE_CODE;
  }

  @Override
  public String getColumnTypeName(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isReadOnly(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isWritable(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isDefinitelyWritable(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public String getColumnClassName(int i) throws SQLException {
    return classNames.get(i - 1);
  }

  @Override
  public <T> T unwrap(Class<T> aClass) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isWrapperFor(Class<?> aClass) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }
}
