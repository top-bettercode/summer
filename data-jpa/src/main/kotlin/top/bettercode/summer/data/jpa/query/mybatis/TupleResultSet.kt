package top.bettercode.summer.data.jpa.query.mybatis

import org.hibernate.HibernateException
import top.bettercode.summer.data.jpa.support.JpaUtil
import java.io.*
import java.math.BigDecimal
import java.net.URL
import java.sql.*
import java.sql.Array
import java.sql.Date
import java.util.*
import javax.persistence.*

/**
 * @author Peter Wu
 */
class TupleResultSet(private val tuples: List<Tuple>) : ResultSet {
    private var closed = false
    private var currentRow = 0
    private var currentCloumn = 0
    private val maxRow: Int
    private val resultSetMetaData: TupleResultSetMetaData

    init {
        resultSetMetaData = TupleResultSetMetaData(tuples)
        maxRow = tuples.size
    }

    @Throws(SQLException::class)
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

    @Throws(SQLException::class)
    override fun close() {
        closed = true
    }

    @Throws(SQLException::class)
    override fun wasNull(): Boolean {
        return if (currentCloumn > 0 && currentCloumn <= resultSetMetaData.columnCount) {
            getObject(currentCloumn) == null
        } else {
            true
        }
    }

    @Throws(SQLException::class)
    override fun getString(i: Int): String? {
        return getObject(i, String::class.java)
    }

    @Throws(SQLException::class)
    override fun getBoolean(i: Int): Boolean {
        return getObject(i, Boolean::class.javaPrimitiveType!!)!!
    }

    @Throws(SQLException::class)
    override fun getByte(i: Int): Byte {
        return getObject(i, Byte::class.javaPrimitiveType!!)!!
    }

    @Throws(SQLException::class)
    override fun getShort(i: Int): Short {
        return getObject(i, Short::class.javaPrimitiveType!!)!!
    }

    @Throws(SQLException::class)
    override fun getInt(i: Int): Int {
        return getObject(i, Int::class.javaPrimitiveType!!)!!
    }

    @Throws(SQLException::class)
    override fun getLong(i: Int): Long {
        return getObject(i, Long::class.javaPrimitiveType!!)!!
    }

    @Throws(SQLException::class)
    override fun getFloat(i: Int): Float {
        return getObject(i, Float::class.javaPrimitiveType!!)!!
    }

    @Throws(SQLException::class)
    override fun getDouble(i: Int): Double {
        return getObject(i, Double::class.javaPrimitiveType!!)!!
    }

    @Deprecated("")
    @Throws(SQLException::class)
    override fun getBigDecimal(i: Int, i1: Int): BigDecimal {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getBytes(i: Int): ByteArray? {
        return getObject(i, ByteArray::class.java)
    }

    @Throws(SQLException::class)
    override fun getDate(i: Int): Date? {
        return getObject(i, Date::class.java)
    }

    @Throws(SQLException::class)
    override fun getTime(i: Int): Time? {
        return getObject(i, Time::class.java)
    }

    @Throws(SQLException::class)
    override fun getTimestamp(i: Int): Timestamp? {
        return getObject(i, Timestamp::class.java)
    }

    @Throws(SQLException::class)
    override fun getAsciiStream(i: Int): InputStream {
        throw SQLFeatureNotSupportedException()
    }

    @Deprecated("")
    @Throws(SQLException::class)
    override fun getUnicodeStream(i: Int): InputStream {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getBinaryStream(i: Int): InputStream {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getString(s: String): String? {
        return getObject(s, String::class.java)
    }

    @Throws(SQLException::class)
    override fun getBoolean(s: String): Boolean {
        return getObject(s, Boolean::class.javaPrimitiveType!!)!!
    }

    @Throws(SQLException::class)
    override fun getByte(s: String): Byte {
        return getObject(s, Byte::class.javaPrimitiveType!!)!!
    }

    @Throws(SQLException::class)
    override fun getShort(s: String): Short {
        return getObject(s, Short::class.javaPrimitiveType!!)!!
    }

    @Throws(SQLException::class)
    override fun getInt(s: String): Int {
        return getObject(s, Int::class.javaPrimitiveType!!)!!
    }

    @Throws(SQLException::class)
    override fun getLong(s: String): Long {
        return getObject(s, Long::class.javaPrimitiveType!!)!!
    }

    @Throws(SQLException::class)
    override fun getFloat(s: String): Float {
        return getObject(s, Float::class.javaPrimitiveType!!)!!
    }

    @Throws(SQLException::class)
    override fun getDouble(s: String): Double {
        return getObject(s, Double::class.javaPrimitiveType!!)!!
    }

    @Deprecated("")
    @Throws(SQLException::class)
    override fun getBigDecimal(s: String, i: Int): BigDecimal {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getBytes(s: String): ByteArray? {
        return getObject(s, ByteArray::class.java)
    }

    @Throws(SQLException::class)
    override fun getDate(s: String): Date? {
        return getObject(s, Date::class.java)
    }

    @Throws(SQLException::class)
    override fun getTime(s: String): Time ?{
        return getObject(s, Time::class.java)
    }

    @Throws(SQLException::class)
    override fun getTimestamp(s: String): Timestamp? {
        return getObject(s, Timestamp::class.java)
    }

    @Throws(SQLException::class)
    override fun getAsciiStream(s: String): InputStream {
        throw SQLFeatureNotSupportedException()
    }

    @Deprecated("")
    @Throws(SQLException::class)
    override fun getUnicodeStream(s: String): InputStream {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getBinaryStream(s: String): InputStream {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getWarnings(): SQLWarning {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun clearWarnings() {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getCursorName(): String {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getMetaData(): ResultSetMetaData {
        return resultSetMetaData
    }

    @Throws(SQLException::class)
    override fun getObject(i: Int): Any? {
        currentCloumn = i
        return tuples[currentRow - 1][i]
    }

    @Throws(SQLException::class)
    override fun getObject(s: String): Any? {
        currentCloumn = resultSetMetaData.findColumn(s)
        return tuples[currentRow - 1][s]
    }

    @Throws(SQLException::class)
    override fun findColumn(s: String): Int {
        return resultSetMetaData.findColumn(s)
    }

    @Throws(SQLException::class)
    override fun getCharacterStream(i: Int): Reader? {
        return getObject(i, Reader::class.java)
    }

    @Throws(SQLException::class)
    override fun getCharacterStream(s: String): Reader? {
        return getObject(s, Reader::class.java)
    }

    @Throws(SQLException::class)
    override fun getBigDecimal(i: Int): BigDecimal? {
        return getObject(i, BigDecimal::class.java)
    }

    @Throws(SQLException::class)
    override fun getBigDecimal(s: String): BigDecimal? {
        return getObject(s, BigDecimal::class.java)
    }

    @Throws(SQLException::class)
    override fun isBeforeFirst(): Boolean {
        return currentRow < 1
    }

    @Throws(SQLException::class)
    override fun isAfterLast(): Boolean {
        return currentRow > maxRow
    }

    @Throws(SQLException::class)
    override fun isFirst(): Boolean {
        return currentRow == 1
    }

    @Throws(SQLException::class)
    override fun isLast(): Boolean {
        return currentRow == maxRow
    }

    @Throws(SQLException::class)
    override fun beforeFirst() {
        currentRow = 0
    }

    @Throws(SQLException::class)
    override fun afterLast() {
        last()
        next()
    }

    @Throws(SQLException::class)
    override fun first(): Boolean {
        beforeFirst()
        return next()
    }

    @Throws(SQLException::class)
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

    @Throws(SQLException::class)
    override fun getRow(): Int {
        return currentRow
    }

    @Throws(SQLException::class)
    override fun absolute(row: Int): Boolean {
        if (row <= 0 || row > maxRow) {
            return false
        }
        currentRow = row - 1
        return next()
    }

    @Throws(SQLException::class)
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

    @Throws(SQLException::class)
    override fun previous(): Boolean {
        if (currentRow <= 1) {
            currentRow = 0
            return false
        }
        currentRow--
        return true
    }

    @Throws(SQLException::class)
    override fun setFetchDirection(i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getFetchDirection(): Int {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun setFetchSize(i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getFetchSize(): Int {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getType(): Int {
        return ResultSet.TYPE_FORWARD_ONLY
    }

    @Throws(SQLException::class)
    override fun getConcurrency(): Int {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun rowUpdated(): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun rowInserted(): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun rowDeleted(): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateNull(i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBoolean(i: Int, b: Boolean) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateByte(i: Int, b: Byte) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateShort(i: Int, i1: Short) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateInt(i: Int, i1: Int) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateLong(i: Int, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateFloat(i: Int, v: Float) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateDouble(i: Int, v: Double) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBigDecimal(i: Int, bigDecimal: BigDecimal) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateString(i: Int, s: String) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBytes(i: Int, bytes: ByteArray) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateDate(i: Int, date: Date) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateTime(i: Int, time: Time) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateTimestamp(i: Int, timestamp: Timestamp) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateAsciiStream(i: Int, inputStream: InputStream, i1: Int) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBinaryStream(i: Int, inputStream: InputStream, i1: Int) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateCharacterStream(i: Int, reader: Reader, i1: Int) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateObject(i: Int, o: Any, i1: Int) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateObject(i: Int, o: Any) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateNull(s: String) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBoolean(s: String, b: Boolean) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateByte(s: String, b: Byte) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateShort(s: String, i: Short) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateInt(s: String, i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateLong(s: String, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateFloat(s: String, v: Float) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateDouble(s: String, v: Double) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBigDecimal(s: String, bigDecimal: BigDecimal) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateString(s: String, s1: String) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBytes(s: String, bytes: ByteArray) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateDate(s: String, date: Date) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateTime(s: String, time: Time) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateTimestamp(s: String, timestamp: Timestamp) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateAsciiStream(s: String, inputStream: InputStream, i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBinaryStream(s: String, inputStream: InputStream, i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateCharacterStream(s: String, reader: Reader, i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateObject(s: String, o: Any, i: Int) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateObject(s: String, o: Any) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun insertRow() {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateRow() {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun deleteRow() {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun refreshRow() {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun cancelRowUpdates() {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun moveToInsertRow() {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun moveToCurrentRow() {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getStatement(): Statement {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getObject(i: Int, map: Map<String?, Class<*>?>?): Any {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getRef(i: Int): Ref {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getBlob(i: Int): Blob {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getClob(i: Int): Clob {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getArray(i: Int): Array {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getObject(s: String, map: Map<String?, Class<*>?>?): Any {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getRef(s: String): Ref {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getBlob(s: String): Blob {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getClob(s: String): Clob {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getArray(s: String): Array {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getDate(i: Int, calendar: Calendar): Date? {
        return getObject(i, Date::class.java)
    }

    @Throws(SQLException::class)
    override fun getDate(s: String, calendar: Calendar): Date? {
        return getObject(s, Date::class.java)
    }

    @Throws(SQLException::class)
    override fun getTime(i: Int, calendar: Calendar): Time? {
        return getObject(i, Time::class.java)
    }

    @Throws(SQLException::class)
    override fun getTime(s: String, calendar: Calendar): Time? {
        return getObject(s, Time::class.java)
    }

    @Throws(SQLException::class)
    override fun getTimestamp(i: Int, calendar: Calendar): Timestamp? {
        return getObject(i, Timestamp::class.java)
    }

    @Throws(SQLException::class)
    override fun getTimestamp(s: String, calendar: Calendar): Timestamp? {
        return getObject(s, Timestamp::class.java)
    }

    @Throws(SQLException::class)
    override fun getURL(i: Int): URL? {
        return getObject(i, URL::class.java)
    }

    @Throws(SQLException::class)
    override fun getURL(s: String): URL ?{
        return getObject(s, URL::class.java)
    }

    @Throws(SQLException::class)
    override fun updateRef(i: Int, ref: Ref) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateRef(s: String, ref: Ref) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBlob(i: Int, blob: Blob) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBlob(s: String, blob: Blob) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateClob(i: Int, clob: Clob) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateClob(s: String, clob: Clob) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateArray(i: Int, array: Array) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateArray(s: String, array: Array) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getRowId(i: Int): RowId {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getRowId(s: String): RowId {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateRowId(i: Int, rowId: RowId) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateRowId(s: String, rowId: RowId) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getHoldability(): Int {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun isClosed(): Boolean {
        return closed
    }

    @Throws(SQLException::class)
    override fun updateNString(i: Int, s: String) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateNString(s: String, s1: String) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateNClob(i: Int, nClob: NClob) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateNClob(s: String, nClob: NClob) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getNClob(i: Int): NClob {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getNClob(s: String): NClob {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getSQLXML(i: Int): SQLXML {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getSQLXML(s: String): SQLXML {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateSQLXML(i: Int, sqlxml: SQLXML) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateSQLXML(s: String, sqlxml: SQLXML) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getNString(i: Int): String {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getNString(s: String): String {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getNCharacterStream(i: Int): Reader {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getNCharacterStream(s: String): Reader {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateNCharacterStream(i: Int, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateNCharacterStream(s: String, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateAsciiStream(i: Int, inputStream: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBinaryStream(i: Int, inputStream: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateCharacterStream(i: Int, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateAsciiStream(s: String, inputStream: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBinaryStream(s: String, inputStream: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateCharacterStream(s: String, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBlob(i: Int, inputStream: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBlob(s: String, inputStream: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateClob(i: Int, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateClob(s: String, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateNClob(i: Int, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateNClob(s: String, reader: Reader, l: Long) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateNCharacterStream(i: Int, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateNCharacterStream(s: String, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateAsciiStream(i: Int, inputStream: InputStream) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBinaryStream(i: Int, inputStream: InputStream) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateCharacterStream(i: Int, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateAsciiStream(s: String, inputStream: InputStream) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBinaryStream(s: String, inputStream: InputStream) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateCharacterStream(s: String, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBlob(i: Int, inputStream: InputStream) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateBlob(s: String, inputStream: InputStream) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateClob(i: Int, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateClob(s: String, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateNClob(i: Int, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun updateNClob(s: String, reader: Reader) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun <T> getObject(i: Int, aClass: Class<T>): T? {
        currentCloumn = i
        return JpaUtil.convert(tuples[currentRow - 1][i], aClass)
    }

    @Throws(SQLException::class)
    override fun <T> getObject(s: String, aClass: Class<T>): T? {
        currentCloumn = resultSetMetaData.findColumn(s)
        return JpaUtil.convert(tuples[currentRow - 1][s], aClass)
    }

    @Throws(SQLException::class)
    override fun <T> unwrap(aClass: Class<T>): T {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun isWrapperFor(aClass: Class<*>?): Boolean {
        return false
    }
}
