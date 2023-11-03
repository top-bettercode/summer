package top.bettercode.summer.data.jpa.query.mybatis

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.sql.Date

/**
 *
 * @author Peter Wu
 */
class TupleResultSetMetaDataTest {

    @Test
    fun init() {
        val tuples = listOf(
                NativeTupleImpl(arrayOf(1L, "name1", null, null), arrayOf("id", "name", "date", "createTime")),
                NativeTupleImpl(arrayOf(2L, "name2", null, Date(System.currentTimeMillis())), arrayOf("id", "name", "date", "createTime")),
                NativeTupleImpl(arrayOf(3L, "name3", Date(System.currentTimeMillis()), null), arrayOf("id", "name", "date", "createTime")),
                NativeTupleImpl(arrayOf(4L, "name4", null, null), arrayOf("id", "name", "date", "createTime")),
        )
        val tupleResultSetMetaData = TupleResultSetMetaData(tuples)
        Assertions.assertEquals(tupleResultSetMetaData.columnCount, 4)
        Assertions.assertEquals(tupleResultSetMetaData.findColumn("id"), 0)
        Assertions.assertEquals(tupleResultSetMetaData.findColumn("name"), 1)
        Assertions.assertEquals(tupleResultSetMetaData.findColumn("date"), 2)
        Assertions.assertEquals(tupleResultSetMetaData.findColumn("createTime"), 3)
        Assertions.assertEquals(tupleResultSetMetaData.getColumnLabel(1), "id")
        Assertions.assertEquals(tupleResultSetMetaData.getColumnLabel(2), "name")
        Assertions.assertEquals(tupleResultSetMetaData.getColumnLabel(3), "date")
        Assertions.assertEquals(tupleResultSetMetaData.getColumnLabel(4), "createTime")
        Assertions.assertEquals(tupleResultSetMetaData.getColumnName(1), "id")
        Assertions.assertEquals(tupleResultSetMetaData.getColumnName(2), "name")
        Assertions.assertEquals(tupleResultSetMetaData.getColumnName(3), "date")
        Assertions.assertEquals(tupleResultSetMetaData.getColumnName(4), "createTime")
        Assertions.assertEquals(tupleResultSetMetaData.getColumnType(1), java.sql.Types.BIGINT)
        Assertions.assertEquals(tupleResultSetMetaData.getColumnType(2), java.sql.Types.VARCHAR)
        Assertions.assertEquals(tupleResultSetMetaData.getColumnType(3), java.sql.Types.DATE)
        Assertions.assertEquals(tupleResultSetMetaData.getColumnType(4), java.sql.Types.DATE)
    }
}