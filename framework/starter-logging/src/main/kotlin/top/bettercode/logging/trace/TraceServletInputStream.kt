package top.bettercode.logging.trace

import java.io.ByteArrayOutputStream
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream

class TraceServletInputStream(private val delegate: ServletInputStream, private val byteArrayOutputStream: ByteArrayOutputStream) : ServletInputStream() {

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


    override fun isReady(): Boolean {
        return delegate.isReady
    }

    override fun available(): Int {
        return delegate.available()
    }

    override fun isFinished(): Boolean {
        return delegate.isFinished
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


    override fun setReadListener(listener: ReadListener?) {
        delegate.setReadListener(listener)
    }
}