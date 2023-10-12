package top.bettercode.summer.data.jpa.query.mybatis

import org.hibernate.HibernateException
import top.bettercode.summer.data.jpa.support.JpaUtil
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.*
import java.sql.Array
import java.sql.Date
import java.util.*
import javax.persistence.Tuple

/**
 * @author Peter Wu
 */
class TupleResultSet(private val tuples: List<Tuple>) : ResultSet {
    private var closed = false
    private var currentRow = 0
    private var currentCloumn = 0
    private val maxRow: Int = tuples.size
    private val resultSetMetaData: TupleResultSetMetaData = TupleResultSetMetaData(tuples)

    override fun next(): Boolean {
        if (maxRow == 0) {
            currentRow = 0
            return false
        }
        if (maxRow <= currentRow) {
            currentRow = maxRow + 1
            return false
        }
        currentRow++
        return true
    }

    override fun close() {
        closed = true
    }

    override fun wasNull(): Boolean {
        return if (currentCloumn > 0 && currentCloumn <= resultSetMetaData.columnCount) {
            getObject(currentCloumn) == null
        } else {
            true
        }
    }

    override fun getString(i: Int): String? {
        return getObject(i, String::class.java)
    }

    override fun getBoolean(i: Int): Boolean {
        return getObject(i, Boolean::class.java)!!
    }

    override fun getByte(i: Int): Byte {
        return getObject(i, Byte::class.java)!!
    }

    override fun getShort(i: Int): Short {
        return getObject(i, Short::class.java)!!
    }

    override fun getInt(i: Int): Int {
        return getObject(i, Int::class.java)!!
    }

    override fun getLong(i: Int): Long {
        return getObject(i, Long::class.java)!!
    }

    override fun getFloat(i: Int): Float {
        return getObject(i, Float::class.java)!!
    }

    override fun getDouble(i: Int): Double {
        return getObject(i, Double::class.java)!!
    }

    @Deprecated("")
    override fun getBigDecimal(i: Int, i1: Int): BigDecimal {
        throw SQLFeatureNotSupportedException()
    }

    override fun getBytes(i: Int): ByteArray? {
        return getObject(i, ByteArray::class.java)
    }

    override fun getDate(i: Int): Date? {
        return getObject(i, Date::class.java)
    }

    override fun getTime(i: Int): Time? {
        return getObject(i, Time::class.java)
    }

    override fun getTimestamp(i: Int): Timestamp? {
        return getObject(i, Timestamp::class.java)
    }

    override fun getAsciiStream(i: Int): InputStream {
        throw SQLFeatureNotSupportedException()
    }

    @Deprecated("")
    override fun getUnicodeStream(i: Int): InputStream {
        throw SQLFeatureNotSupportedException()
    }

    override fun getBinaryStream(i: Int): InputStream {
        throw SQLFeatureNotSupportedException()
    }

    override fun getString(s: String): String? {
        return getObject(s, String::class.java)
    }

    override fun getBoolean(s: String): Boolean {
        return getObject(s, Boolean::class.java)!!
    }

    override fun getByte(s: String): Byte {
        return getObject(s, Byte::class.java)!!
    }

    override fun getShort(s: String): Short {
        return getObject(s, Short::class.java)!!
    }

    override fun getInt(s: String): Int {
        return getObject(s, Int::class.java)!!
    }

    override fun getLong(s: String): Long {
        return getObject(s, Long::class.java)!!
    }

    override fun getFloat(s: String): Float {
        return getObject(s, Float::class.java)!!
    }

    override fun getDouble(s: String): Double {
        return getObject(s, Double::class.java)!!
    }

    @Deprecated("")
    override fun getBigDecimal(s: String, i: Int): BigDecimal {
        throw SQLFeatureNotSupportedException()
    }

    override fun getBytes(s: String): ByteArray? {
        return getObject(s, ByteArray::class.java)
    }

    override fun getDate(s: String): Date? {
        return getObject(s, Date::class.java)
    }

    override fun getTime(s: String): Time? {
        return getObject(s, Time::class.java)
    }

    override fun getTimestamp(s: String): Timestamp? {
        return getObject(s, Timestamp::class.java)
    }

    override fun getAsciiStream(s: String): InputStream {
        throw SQLFeatureNotSupportedException()
    }

    @Deprecated("")
    override fun getUnicodeStream(s: String): InputStream {
        throw SQLFeatureNotSupportedException()
    }

    override fun getBinaryStream(s: String): InputStream {
        throw SQLFeatureNotSupportedException()
    }

    override fun getWarnings(): SQLWarning {
        throw SQLFeatureNotSupportedException()
    }

    override fun clearWarnings() {
        throw SQLFeatureNotSupportedException()
    }

    override fun getCursorName(): String {
        throw SQLFeatureNotSupportedException()
    }

    override fun getMetaData(): ResultSetMetaData {
        return resultSetMetaData
    }

    override fun getObject(i: Int): Any? {
        currentCloumn = i
        return tuples[currentRow - 1][i]
    }

    override fun getObject(s: String): Any? {
        currentCloumn = resultSetMetaData.findColumn(s)
        return tuples[currentRow - 1][s]
    }

    override fun findColumn(s: String): Int {
        return resultSetMetaData.findColumn(s)
    }

    override fun getCharacterStream(i: Int): Reader? {
        return getObject(i, Reader::class.java)
    }

    override fun getCharacterStream(s: String): Reader? {
        return getObject(s, Reader::class.java)
    }

    override fun getBigDecimal(i: Int): BigDecimal? {
        return getObject(i, BigDecimal::class.java)
    }

    override fun getBigDecimal(s: String): BigDecimal? {
        return getObject(s, BigDecimal::class.java)
    }

    override fun isBeforeFirst(): Boolean {
        return currentRow < 1
    }

    override fun isAfterLast(): Boolean {
        return currentRow > maxRow
    }

    override fun isFirst(): Boolean {
        return currentRow == 1
    }

    override fun isLast(): Boolean {
        return currentRow == maxRow
    }

    override fun beforeFirst() {
        currentRow = 0
    }

    override fun afterLast() {
        last()
        next()
    }

    override fun first(): Boolean {
        beforeFirst()
        return next()
    }

    override fun last(): Boolean {
        var more = false
        if (currentRow > maxRow) {
            more = previous()
        }
        for (i in currentRow until maxRow) {
            more = next()
        }
        return more
    }

    override fun getRow(): Int {
        return currentRow
    }

    override fun absolute(row: Int): Boolean {
        if (row <= 0 || row > maxRow) {
            return false
        }
        currentRow = row - 1
        return next()
    }

    override fun relative(row: Int): Boolean {
        var more = false
        if (row > 0) {
            // scroll ahead
            for (i in 0 until row) {
                more = next()
                if (!more) {
                    break
                }
            }
        } else if (row < 0) {
            // scroll backward
            for (i in 0 until -row) {
                more = previous()
                if (!more) {
                    break
                }
            }
        } else {
            throw HibernateException("scroll(0) not valid")
        }
        return more
    }

    override fun previous(): Boolean {
        if (currentRow <= 1) {
            currentRow = 0
            return false
        }
        currentRow--
        return true
    }

    override fun setFetchDirection(i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    override fun getFetchDirection(): Int {
        throw SQLFeatureNotSupportedException()
    }

    override fun setFetchSize(i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    override fun getFetchSize(): Int {
        throw SQLFeatureNotSupportedException()
    }

    override fun getType(): Int {
        return ResultSet.TYPE_FORWARD_ONLY
    }

    override fun getConcurrency(): Int {
        throw SQLFeatureNotSupportedException()
    }

    override fun rowUpdated(): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    override fun rowInserted(): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    override fun rowDeleted(): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateNull(i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBoolean(i: Int, b: Boolean) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateByte(i: Int, b: Byte) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateShort(i: Int, i1: Short) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateInt(i: Int, i1: Int) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateLong(i: Int, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateFloat(i: Int, v: Float) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateDouble(i: Int, v: Double) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBigDecimal(i: Int, bigDecimal: BigDecimal) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateString(i: Int, s: String) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBytes(i: Int, bytes: ByteArray) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateDate(i: Int, date: Date) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateTime(i: Int, time: Time) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateTimestamp(i: Int, timestamp: Timestamp) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateAsciiStream(i: Int, inputStream: InputStream, i1: Int) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBinaryStream(i: Int, inputStream: InputStream, i1: Int) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateCharacterStream(i: Int, reader: Reader, i1: Int) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateObject(i: Int, o: Any, i1: Int) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateObject(i: Int, o: Any) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateNull(s: String) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBoolean(s: String, b: Boolean) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateByte(s: String, b: Byte) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateShort(s: String, i: Short) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateInt(s: String, i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateLong(s: String, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateFloat(s: String, v: Float) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateDouble(s: String, v: Double) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBigDecimal(s: String, bigDecimal: BigDecimal) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateString(s: String, s1: String) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBytes(s: String, bytes: ByteArray) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateDate(s: String, date: Date) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateTime(s: String, time: Time) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateTimestamp(s: String, timestamp: Timestamp) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateAsciiStream(s: String, inputStream: InputStream, i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBinaryStream(s: String, inputStream: InputStream, i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateCharacterStream(s: String, reader: Reader, i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateObject(s: String, o: Any, i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateObject(s: String, o: Any) {
        throw SQLFeatureNotSupportedException()
    }

    override fun insertRow() {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateRow() {
        throw SQLFeatureNotSupportedException()
    }

    override fun deleteRow() {
        throw SQLFeatureNotSupportedException()
    }

    override fun refreshRow() {
        throw SQLFeatureNotSupportedException()
    }

    override fun cancelRowUpdates() {
        throw SQLFeatureNotSupportedException()
    }

    override fun moveToInsertRow() {
        throw SQLFeatureNotSupportedException()
    }

    override fun moveToCurrentRow() {
        throw SQLFeatureNotSupportedException()
    }

    override fun getStatement(): Statement {
        throw SQLFeatureNotSupportedException()
    }

    override fun getObject(i: Int, map: Map<String?, Class<*>?>?): Any {
        throw SQLFeatureNotSupportedException()
    }

    override fun getRef(i: Int): Ref {
        throw SQLFeatureNotSupportedException()
    }

    override fun getBlob(i: Int): Blob {
        throw SQLFeatureNotSupportedException()
    }

    override fun getClob(i: Int): Clob {
        throw SQLFeatureNotSupportedException()
    }

    override fun getArray(i: Int): Array {
        throw SQLFeatureNotSupportedException()
    }

    override fun getObject(s: String, map: Map<String?, Class<*>?>?): Any {
        throw SQLFeatureNotSupportedException()
    }

    override fun getRef(s: String): Ref {
        throw SQLFeatureNotSupportedException()
    }

    override fun getBlob(s: String): Blob {
        throw SQLFeatureNotSupportedException()
    }

    override fun getClob(s: String): Clob {
        throw SQLFeatureNotSupportedException()
    }

    override fun getArray(s: String): Array {
        throw SQLFeatureNotSupportedException()
    }

    override fun getDate(i: Int, calendar: Calendar): Date? {
        return getObject(i, Date::class.java)
    }

    override fun getDate(s: String, calendar: Calendar): Date? {
        return getObject(s, Date::class.java)
    }

    override fun getTime(i: Int, calendar: Calendar): Time? {
        return getObject(i, Time::class.java)
    }

    override fun getTime(s: String, calendar: Calendar): Time? {
        return getObject(s, Time::class.java)
    }

    override fun getTimestamp(i: Int, calendar: Calendar): Timestamp? {
        return getObject(i, Timestamp::class.java)
    }

    override fun getTimestamp(s: String, calendar: Calendar): Timestamp? {
        return getObject(s, Timestamp::class.java)
    }

    override fun getURL(i: Int): URL? {
        return getObject(i, URL::class.java)
    }

    override fun getURL(s: String): URL? {
        return getObject(s, URL::class.java)
    }

    override fun updateRef(i: Int, ref: Ref) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateRef(s: String, ref: Ref) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBlob(i: Int, blob: Blob) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBlob(s: String, blob: Blob) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateClob(i: Int, clob: Clob) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateClob(s: String, clob: Clob) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateArray(i: Int, array: Array) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateArray(s: String, array: Array) {
        throw SQLFeatureNotSupportedException()
    }

    override fun getRowId(i: Int): RowId {
        throw SQLFeatureNotSupportedException()
    }

    override fun getRowId(s: String): RowId {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateRowId(i: Int, rowId: RowId) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateRowId(s: String, rowId: RowId) {
        throw SQLFeatureNotSupportedException()
    }

    override fun getHoldability(): Int {
        throw SQLFeatureNotSupportedException()
    }

    override fun isClosed(): Boolean {
        return closed
    }

    override fun updateNString(i: Int, s: String) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateNString(s: String, s1: String) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateNClob(i: Int, nClob: NClob) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateNClob(s: String, nClob: NClob) {
        throw SQLFeatureNotSupportedException()
    }

    override fun getNClob(i: Int): NClob {
        throw SQLFeatureNotSupportedException()
    }

    override fun getNClob(s: String): NClob {
        throw SQLFeatureNotSupportedException()
    }

    override fun getSQLXML(i: Int): SQLXML {
        throw SQLFeatureNotSupportedException()
    }

    override fun getSQLXML(s: String): SQLXML {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateSQLXML(i: Int, sqlxml: SQLXML) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateSQLXML(s: String, sqlxml: SQLXML) {
        throw SQLFeatureNotSupportedException()
    }

    override fun getNString(i: Int): String {
        throw SQLFeatureNotSupportedException()
    }

    override fun getNString(s: String): String {
        throw SQLFeatureNotSupportedException()
    }

    override fun getNCharacterStream(i: Int): Reader {
        throw SQLFeatureNotSupportedException()
    }

    override fun getNCharacterStream(s: String): Reader {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateNCharacterStream(i: Int, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateNCharacterStream(s: String, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateAsciiStream(i: Int, inputStream: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBinaryStream(i: Int, inputStream: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateCharacterStream(i: Int, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateAsciiStream(s: String, inputStream: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBinaryStream(s: String, inputStream: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateCharacterStream(s: String, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBlob(i: Int, inputStream: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBlob(s: String, inputStream: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateClob(i: Int, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateClob(s: String, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateNClob(i: Int, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateNClob(s: String, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateNCharacterStream(i: Int, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateNCharacterStream(s: String, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateAsciiStream(i: Int, inputStream: InputStream) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBinaryStream(i: Int, inputStream: InputStream) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateCharacterStream(i: Int, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateAsciiStream(s: String, inputStream: InputStream) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBinaryStream(s: String, inputStream: InputStream) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateCharacterStream(s: String, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBlob(i: Int, inputStream: InputStream) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateBlob(s: String, inputStream: InputStream) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateClob(i: Int, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateClob(s: String, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateNClob(i: Int, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    override fun updateNClob(s: String, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    override fun <T> getObject(i: Int, aClass: Class<T>): T? {
        currentCloumn = i
        return JpaUtil.convert(tuples[currentRow - 1][i], aClass)
    }

    override fun <T> getObject(s: String, aClass: Class<T>): T? {
        currentCloumn = resultSetMetaData.findColumn(s)
        return JpaUtil.convert(tuples[currentRow - 1][s], aClass)
    }

    override fun <T> unwrap(aClass: Class<T>): T {
        throw SQLFeatureNotSupportedException()
    }

    override fun isWrapperFor(aClass: Class<*>?): Boolean {
        return false
    }
}
