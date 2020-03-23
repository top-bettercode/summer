package java.io

/**
 * @author Peter Wu
 */
class TraceBufferedReader(private val delegate: BufferedReader, private val byteArrayOutputStream: ByteArrayOutputStream) : BufferedReader(delegate) {

    override fun equals(other: Any?): Boolean {
        return delegate == other
    }

    override fun hashCode(): Int {
        return delegate.hashCode()
    }

    override fun toString(): String {
        return delegate.toString()
    }

    override fun skip(n: Long): Long {
        return delegate.skip(n)
    }

    override fun readLine(ignoreLF: Boolean): String {
        val readLine = delegate.readLine(ignoreLF)
        byteArrayOutputStream.write(readLine.toByteArray())
        byteArrayOutputStream.write("\n".toByteArray())
        return readLine
    }

    override fun ready(): Boolean {
        return delegate.ready()
    }

    override fun reset() {
        delegate.reset()
    }

    override fun close() {
        delegate.close()
    }

    override fun markSupported(): Boolean {
        return delegate.markSupported()
    }

    override fun mark(readAheadLimit: Int) {
        delegate.mark(readAheadLimit)
    }

    override fun read(): Int {
        val read = delegate.read()
        if (read != -1)
            byteArrayOutputStream.write(read)
        return read
    }

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        val read = delegate.read(cbuf, off, len)
        if (read != -1)
            byteArrayOutputStream.write(String(cbuf.copyOfRange(off, off + read)).toByteArray())
        return read
    }

}
