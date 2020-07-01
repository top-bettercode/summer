package cn.bestwu.logging.trace

import java.io.ByteArrayOutputStream
import java.io.InputStream

class TraceInputStream(private val delegate: InputStream, private val byteArrayOutputStream: ByteArrayOutputStream) : InputStream() {

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

    override fun available(): Int {
        return delegate.available()
    }

    override fun reset() {
        delegate.reset()
    }

    override fun close() {
        delegate.close()
    }

    override fun mark(readlimit: Int) {
        delegate.mark(readlimit)
    }

    override fun markSupported(): Boolean {
        return delegate.markSupported()
    }

    override fun read(): Int {
        val read = delegate.read()
        if (read != -1) {
            byteArrayOutputStream.write(read)
        }
        return read
    }
}