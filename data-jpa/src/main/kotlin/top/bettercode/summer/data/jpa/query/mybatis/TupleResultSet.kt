package top.bettercode.summer.data.jpa.query.mybatis

import jakarta.persistence.Tuple
import org.hibernate.HibernateException
import top.bettercode.summer.data.jpa.support.JpaUtil
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import java.sql.*
import java.sql.Array
import java.sql.Date
import java.util.*
import javax.sql.rowset.serial.SerialBlob
import javax.sql.rowset.serial.SerialClob

/**
 * @author Peter Wu
 */
class TupleResultSet(private val tuples: List<Tuple>,
                     private val resultSetMetaData: TupleResultSetMetaData) : ResultSet {
    private var closed = false
    private var currentRow = 0
    private var currentCloumn = 0
    private val maxRow: Int = tuples.size

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
        return if (currentCloumn >= 0 && currentCloumn <= resultSetMetaData.columnCount) {
            getObject(currentCloumn) == null
        } else {
            true
        }
    }

    override fun getString(i: Int): String? {
        return getObject(i, String::class.java)
    }

    override fun getBoolean(i: Int): Boolean {
        return getObject(i, Boolean::class.java) ?: false
    }

    override fun getByte(i: Int): Byte {
        return getObject(i, Byte::class.java) ?: 0
    }

    override fun getShort(i: Int): Short {
        return getObject(i, Short::class.java) ?: 0
    }

    override fun getInt(i: Int): Int {
        return getObject(i, Int::class.java) ?: 0
    }

    override fun getLong(i: Int): Long {
        return getObject(i, Long::class.java) ?: 0L
    }

    override fun getFloat(i: Int): Float {
        return getObject(i, Float::class.java) ?: 0F
    }

    override fun getDouble(i: Int): Double {
        return getObject(i, Double::class.java) ?: 0.0
    }

    @Deprecated("", ReplaceWith("getObject(i, BigDecimal::class.java)?.setScale(scale, RoundingMode.HALF_UP)", "java.math.BigDecimal"))
    override fun getBigDecimal(i: Int, scale: Int): BigDecimal? {
        return getObject(i, BigDecimal::class.java)?.setScale(scale, RoundingMode.HALF_UP)
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

    override fun getAsciiStream(i: Int): InputStream? {
        return getObject(i, ByteArray::class.java)?.inputStream()
    }

    @Deprecated("", ReplaceWith("getObject(i, ByteArray::class.java)?.inputStream()"))
    override fun getUnicodeStream(i: Int): InputStream? {
        return getObject(i, ByteArray::class.java)?.inputStream()
    }

    override fun getBinaryStream(i: Int): InputStream? {
        return getObject(i, ByteArray::class.java)?.inputStream()
    }

    override fun getString(s: String): String? {
        return getObject(s, String::class.java)
    }

    override fun getBoolean(s: String): Boolean {
        return getObject(s, Boolean::class.java) ?: false
    }

    override fun getByte(s: String): Byte {
        return getObject(s, Byte::class.java) ?: 0
    }

    override fun getShort(s: String): Short {
        return getObject(s, Short::class.java) ?: 0
    }

    override fun getInt(s: String): Int {
        return getObject(s, Int::class.java) ?: 0
    }

    override fun getLong(s: String): Long {
        return getObject(s, Long::class.java) ?: 0L
    }

    override fun getFloat(s: String): Float {
        return getObject(s, Float::class.java) ?: 0F
    }

    override fun getDouble(s: String): Double {
        return getObject(s, Double::class.java) ?: 0.0
    }

    @Deprecated("", ReplaceWith("getObject(s, BigDecimal::class.java)?.setScale(scale, RoundingMode.HALF_UP)", "java.math.BigDecimal"))
    override fun getBigDecimal(s: String, scale: Int): BigDecimal? {
        return getObject(s, BigDecimal::class.java)?.setScale(scale, RoundingMode.HALF_UP)
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

    override fun getAsciiStream(s: String): InputStream? {
        return getObject(s, ByteArray::class.java)?.inputStream()
    }

    @Deprecated("", ReplaceWith("getObject(s, ByteArray::class.java)?.inputStream()"))
    override fun getUnicodeStream(s: String): InputStream? {
        return getObject(s, ByteArray::class.java)?.inputStream()
    }

    override fun getBinaryStream(s: String): InputStream? {
        return getObject(s, ByteArray::class.java)?.inputStream()
    }

    override fun getWarnings(): SQLWarning? {
        return null
    }

    override fun clearWarnings() {
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
        return getObject(i, ByteArray::class.java)?.inputStream()?.reader()
    }

    override fun getCharacterStream(s: String): Reader? {
        return getObject(s, ByteArray::class.java)?.inputStream()?.reader()
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
    }

    override fun getFetchSize(): Int {
        return maxRow
    }

    override fun getType(): Int {
        return ResultSet.TYPE_FORWARD_ONLY
    }

    override fun getConcurrency(): Int {
        return ResultSet.CONCUR_READ_ONLY
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

    override fun getObject(i: Int, map: Map<String?, Class<*>?>?): Any? {
        return getObject(i)
    }

    override fun getRef(i: Int): Ref? {
        throw SQLFeatureNotSupportedException()
    }

    override fun getBlob(i: Int): Blob? {
        return getObject(i, ByteArray::class.java)?.let { return SerialBlob(it) }
    }

    override fun getClob(i: Int): Clob? {
        return getObject(i, String::class.java)?.let { return SerialClob(it.toCharArray()) }
    }

    override fun getArray(i: Int): Array? {
        throw SQLFeatureNotSupportedException()
    }

    override fun getObject(s: String, map: Map<String?, Class<*>?>?): Any? {
        return getObject(s)
    }

    override fun getRef(s: String): Ref? {
        throw SQLFeatureNotSupportedException()
    }

    override fun getBlob(s: String): Blob? {
        return getObject(s, ByteArray::class.java)?.let { return SerialBlob(it) }
    }

    override fun getClob(s: String): Clob? {
        return getObject(s, String::class.java)?.let { return SerialClob(it.toCharArray()) }
    }

    override fun getArray(s: String): Array? {
        throw SQLFeatureNotSupportedException()
    }

    private fun convert(original: Date, targetTimeZone: TimeZone): Date {
        val utilDate = Date(original.time)
        val calendar = Calendar.getInstance()
        calendar.setTime(utilDate)
        calendar.setTimeZone(targetTimeZone)
        return Date(calendar.time.time)
    }

    override fun getDate(i: Int, calendar: Calendar): Date? {
        return getObject(i, Date::class.java)?.let { convert(it, calendar.timeZone) }
    }

    override fun getDate(s: String, calendar: Calendar): Date? {
        return getObject(s, Date::class.java)?.let { convert(it, calendar.timeZone) }
    }

    private fun convert(original: Time, targetTimeZone: TimeZone): Time {
        val utilDate = Date(original.time)
        val calendar = Calendar.getInstance()
        calendar.setTime(utilDate)
        calendar.setTimeZone(targetTimeZone)
        return Time(calendar.time.time)
    }

    override fun getTime(i: Int, calendar: Calendar): Time? {
        return getObject(i, Time::class.java)?.let { convert(it, calendar.timeZone) }
    }

    override fun getTime(s: String, calendar: Calendar): Time? {
        return getObject(s, Time::class.java)?.let { convert(it, calendar.timeZone) }
    }

    private fun convert(original: Timestamp, targetTimeZone: TimeZone): Timestamp {
        val utilDate = Date(original.time)
        val calendar = Calendar.getInstance()
        calendar.setTime(utilDate)
        calendar.setTimeZone(targetTimeZone)
        return Timestamp(calendar.time.time)
    }

    override fun getTimestamp(i: Int, calendar: Calendar): Timestamp? {
        return getObject(i, Timestamp::class.java)?.let { convert(it, calendar.timeZone) }
    }

    override fun getTimestamp(s: String, calendar: Calendar): Timestamp? {
        return getObject(s, Timestamp::class.java)?.let { convert(it, calendar.timeZone) }
    }

    override fun getURL(i: Int): URL? {
        return getObject(i, String::class.java)?.let { URL(it) }
    }

    override fun getURL(s: String): URL? {
        return getObject(s, String::class.java)?.let { URL(it) }
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

    override fun getNClob(i: Int): NClob? {
        return getObject(i, String::class.java)?.let { return object : SerialClob(it.toCharArray()), NClob {} }
    }

    override fun getNClob(s: String): NClob? {
        return getObject(s, String::class.java)?.let { return object : SerialClob(it.toCharArray()), NClob {} }
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

    override fun getNString(i: Int): String? {
        return getString(i)
    }

    override fun getNString(s: String): String? {
        return getString(s)
    }

    override fun getNCharacterStream(i: Int): Reader? {
        return getCharacterStream(i)
    }

    override fun getNCharacterStream(s: String): Reader? {
        return getCharacterStream(s)
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

    override fun <T> unwrap(iface: Class<T>): T {
        return iface.cast(this)
    }

    override fun isWrapperFor(aClass: Class<*>?): Boolean {
        return false
    }
}
