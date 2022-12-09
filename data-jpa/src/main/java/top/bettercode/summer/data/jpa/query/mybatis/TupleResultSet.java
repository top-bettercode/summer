package top.bettercode.summer.data.jpa.query.mybatis;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import javax.persistence.Tuple;
import org.hibernate.HibernateException;
import top.bettercode.summer.data.jpa.support.JpaUtil;

/**
 * @author Peter Wu
 */
public class TupleResultSet implements ResultSet {

  private boolean closed = false;
  private int currentRow = 0;
  private int currentCloumn = 0;
  private final int maxRow;
  private final List<Tuple> tuples;
  private final TupleResultSetMetaData resultSetMetaData;

  public TupleResultSet(List<Tuple> tuples) {
    this.tuples = tuples;
    resultSetMetaData = new TupleResultSetMetaData(tuples);
    this.maxRow = tuples.size();
  }

  @Override
  public boolean next() throws SQLException {
    if (maxRow == 0) {
      currentRow = 0;
      return false;
    }

    if (maxRow <= currentRow) {
      currentRow = maxRow + 1;
      return false;
    }

    currentRow++;

    return true;
  }

  @Override
  public void close() throws SQLException {
    closed = true;
  }

  @Override
  public boolean wasNull() throws SQLException {
    if (currentCloumn > 0 && currentCloumn <= resultSetMetaData.getColumnCount()) {
      return getObject(currentCloumn) == null;
    } else {
      return true;
    }
  }

  @Override
  public String getString(int i) throws SQLException {
    return getObject(i, String.class);
  }

  @Override
  public boolean getBoolean(int i) throws SQLException {
    return getObject(i, boolean.class);
  }

  @Override
  public byte getByte(int i) throws SQLException {
    return getObject(i, byte.class);
  }

  @Override
  public short getShort(int i) throws SQLException {
    return getObject(i, short.class);
  }

  @Override
  public int getInt(int i) throws SQLException {
    return getObject(i, int.class);
  }

  @Override
  public long getLong(int i) throws SQLException {
    return getObject(i, long.class);
  }

  @Override
  public float getFloat(int i) throws SQLException {
    return getObject(i, float.class);
  }

  @Override
  public double getDouble(int i) throws SQLException {
    return getObject(i, double.class);
  }

  @Deprecated
  @Override
  public BigDecimal getBigDecimal(int i, int i1) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public byte[] getBytes(int i) throws SQLException {
    return getObject(i, byte[].class);
  }

  @Override
  public Date getDate(int i) throws SQLException {
    return getObject(i, Date.class);
  }

  @Override
  public Time getTime(int i) throws SQLException {
    return getObject(i, Time.class);
  }

  @Override
  public Timestamp getTimestamp(int i) throws SQLException {
    return getObject(i, Timestamp.class);
  }

  @Override
  public InputStream getAsciiStream(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Deprecated
  @Override
  public InputStream getUnicodeStream(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public InputStream getBinaryStream(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public String getString(String s) throws SQLException {
    return getObject(s, String.class);
  }

  @Override
  public boolean getBoolean(String s) throws SQLException {
    return getObject(s, boolean.class);
  }

  @Override
  public byte getByte(String s) throws SQLException {
    return getObject(s, byte.class);
  }

  @Override
  public short getShort(String s) throws SQLException {
    return getObject(s, short.class);
  }

  @Override
  public int getInt(String s) throws SQLException {
    return getObject(s, int.class);
  }

  @Override
  public long getLong(String s) throws SQLException {
    return getObject(s, long.class);
  }

  @Override
  public float getFloat(String s) throws SQLException {
    return getObject(s, float.class);
  }

  @Override
  public double getDouble(String s) throws SQLException {
    return getObject(s, double.class);
  }

  @Deprecated
  @Override
  public BigDecimal getBigDecimal(String s, int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public byte[] getBytes(String s) throws SQLException {
    return getObject(s, byte[].class);
  }

  @Override
  public Date getDate(String s) throws SQLException {
    return getObject(s, Date.class);
  }

  @Override
  public Time getTime(String s) throws SQLException {
    return getObject(s, Time.class);
  }

  @Override
  public Timestamp getTimestamp(String s) throws SQLException {
    return getObject(s, Timestamp.class);
  }

  @Override
  public InputStream getAsciiStream(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Deprecated
  @Override
  public InputStream getUnicodeStream(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public InputStream getBinaryStream(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void clearWarnings() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public String getCursorName() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    return resultSetMetaData;
  }

  @Override
  public Object getObject(int i) throws SQLException {
    currentCloumn = i;
    return tuples.get(currentRow - 1).get(i);
  }

  @Override
  public Object getObject(String s) throws SQLException {
    currentCloumn = resultSetMetaData.findColumn(s);
    return tuples.get(currentRow - 1).get(s);
  }

  @Override
  public int findColumn(String s) throws SQLException {
    return resultSetMetaData.findColumn(s);
  }

  @Override
  public Reader getCharacterStream(int i) throws SQLException {
    return getObject(i, Reader.class);
  }

  @Override
  public Reader getCharacterStream(String s) throws SQLException {
    return getObject(s, Reader.class);
  }

  @Override
  public BigDecimal getBigDecimal(int i) throws SQLException {
    return getObject(i, BigDecimal.class);
  }

  @Override
  public BigDecimal getBigDecimal(String s) throws SQLException {
    return getObject(s, BigDecimal.class);
  }

  @Override
  public boolean isBeforeFirst() throws SQLException {
    return currentRow < 1;
  }

  @Override
  public boolean isAfterLast() throws SQLException {
    return currentRow > maxRow;
  }

  @Override
  public boolean isFirst() throws SQLException {
    return currentRow == 1;
  }

  @Override
  public boolean isLast() throws SQLException {
    return currentRow == maxRow;
  }

  @Override
  public void beforeFirst() throws SQLException {
    currentRow = 0;
  }

  @Override
  public void afterLast() throws SQLException {
    last();
    next();
  }

  @Override
  public boolean first() throws SQLException {
    beforeFirst();
    return next();
  }

  @Override
  public boolean last() throws SQLException {
    boolean more = false;
    if (currentRow > maxRow) {
      more = previous();
    }
    for (int i = currentRow; i < maxRow; i++) {
      more = next();
    }

    return more;
  }

  @Override
  public int getRow() throws SQLException {
    return currentRow;
  }

  @Override
  public boolean absolute(int row) throws SQLException {
    if (row <= 0 || row > maxRow) {
      return false;
    }
    currentRow = row - 1;
    return next();
  }

  @Override
  public boolean relative(int row) throws SQLException {
    boolean more = false;
    if (row > 0) {
      // scroll ahead
      for (int i = 0; i < row; i++) {
        more = next();
        if (!more) {
          break;
        }
      }
    } else if (row < 0) {
      // scroll backward
      for (int i = 0; i < -row; i++) {
        more = previous();
        if (!more) {
          break;
        }
      }
    } else {
      throw new HibernateException("scroll(0) not valid");
    }

    return more;
  }

  @Override
  public boolean previous() throws SQLException {
    if (currentRow <= 1) {
      currentRow = 0;
      return false;
    }
    currentRow--;
    return true;
  }

  @Override
  public void setFetchDirection(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getFetchDirection() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void setFetchSize(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getFetchSize() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getType() throws SQLException {
    return ResultSet.TYPE_FORWARD_ONLY;
  }

  @Override
  public int getConcurrency() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean rowUpdated() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean rowInserted() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean rowDeleted() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNull(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBoolean(int i, boolean b) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateByte(int i, byte b) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateShort(int i, short i1) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateInt(int i, int i1) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateLong(int i, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateFloat(int i, float v) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateDouble(int i, double v) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBigDecimal(int i, BigDecimal bigDecimal) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateString(int i, String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBytes(int i, byte[] bytes) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateDate(int i, Date date) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateTime(int i, Time time) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateTimestamp(int i, Timestamp timestamp) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateAsciiStream(int i, InputStream inputStream, int i1) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBinaryStream(int i, InputStream inputStream, int i1) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateCharacterStream(int i, Reader reader, int i1) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateObject(int i, Object o, int i1) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateObject(int i, Object o) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNull(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBoolean(String s, boolean b) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateByte(String s, byte b) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateShort(String s, short i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateInt(String s, int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateLong(String s, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateFloat(String s, float v) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateDouble(String s, double v) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBigDecimal(String s, BigDecimal bigDecimal) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateString(String s, String s1) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBytes(String s, byte[] bytes) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateDate(String s, Date date) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateTime(String s, Time time) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateTimestamp(String s, Timestamp timestamp) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateAsciiStream(String s, InputStream inputStream, int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBinaryStream(String s, InputStream inputStream, int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateCharacterStream(String s, Reader reader, int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateObject(String s, Object o, int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateObject(String s, Object o) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void insertRow() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateRow() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void deleteRow() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void refreshRow() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void cancelRowUpdates() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void moveToInsertRow() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void moveToCurrentRow() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Statement getStatement() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Ref getRef(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Blob getBlob(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Clob getClob(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Array getArray(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Object getObject(String s, Map<String, Class<?>> map) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Ref getRef(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Blob getBlob(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Clob getClob(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Array getArray(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Date getDate(int i, Calendar calendar) throws SQLException {
    return getObject(i, Date.class);
  }

  @Override
  public Date getDate(String s, Calendar calendar) throws SQLException {
    return getObject(s, Date.class);
  }

  @Override
  public Time getTime(int i, Calendar calendar) throws SQLException {
    return getObject(i, Time.class);
  }

  @Override
  public Time getTime(String s, Calendar calendar) throws SQLException {
    return getObject(s, Time.class);
  }

  @Override
  public Timestamp getTimestamp(int i, Calendar calendar) throws SQLException {
    return getObject(i, Timestamp.class);
  }

  @Override
  public Timestamp getTimestamp(String s, Calendar calendar) throws SQLException {
    return getObject(s, Timestamp.class);
  }

  @Override
  public URL getURL(int i) throws SQLException {
    return getObject(i, URL.class);
  }

  @Override
  public URL getURL(String s) throws SQLException {
    return getObject(s, URL.class);
  }

  @Override
  public void updateRef(int i, Ref ref) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateRef(String s, Ref ref) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBlob(int i, Blob blob) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBlob(String s, Blob blob) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateClob(int i, Clob clob) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateClob(String s, Clob clob) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateArray(int i, Array array) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateArray(String s, Array array) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public RowId getRowId(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public RowId getRowId(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateRowId(int i, RowId rowId) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateRowId(String s, RowId rowId) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getHoldability() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isClosed() throws SQLException {
    return closed;
  }

  @Override
  public void updateNString(int i, String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNString(String s, String s1) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNClob(int i, NClob nClob) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNClob(String s, NClob nClob) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public NClob getNClob(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public NClob getNClob(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public SQLXML getSQLXML(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public SQLXML getSQLXML(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateSQLXML(int i, SQLXML sqlxml) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateSQLXML(String s, SQLXML sqlxml) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public String getNString(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public String getNString(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Reader getNCharacterStream(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Reader getNCharacterStream(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNCharacterStream(int i, Reader reader, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNCharacterStream(String s, Reader reader, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateAsciiStream(int i, InputStream inputStream, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBinaryStream(int i, InputStream inputStream, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateCharacterStream(int i, Reader reader, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateAsciiStream(String s, InputStream inputStream, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBinaryStream(String s, InputStream inputStream, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateCharacterStream(String s, Reader reader, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBlob(int i, InputStream inputStream, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBlob(String s, InputStream inputStream, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateClob(int i, Reader reader, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateClob(String s, Reader reader, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNClob(int i, Reader reader, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNClob(String s, Reader reader, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNCharacterStream(int i, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNCharacterStream(String s, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateAsciiStream(int i, InputStream inputStream) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBinaryStream(int i, InputStream inputStream) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateCharacterStream(int i, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateAsciiStream(String s, InputStream inputStream) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBinaryStream(String s, InputStream inputStream) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateCharacterStream(String s, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBlob(int i, InputStream inputStream) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBlob(String s, InputStream inputStream) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateClob(int i, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateClob(String s, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNClob(int i, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNClob(String s, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public <T> T getObject(int i, Class<T> aClass) throws SQLException {
    currentCloumn = i;
    return JpaUtil.convert(tuples.get(currentRow - 1).get(i), aClass);
  }

  @Override
  public <T> T getObject(String s, Class<T> aClass) throws SQLException {
    currentCloumn = resultSetMetaData.findColumn(s);
    return JpaUtil.convert(tuples.get(currentRow - 1).get(s), aClass);
  }

  @Override
  public <T> T unwrap(Class<T> aClass) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isWrapperFor(Class<?> aClass) throws SQLException {
    return false;
  }

}
