package top.bettercode.summer.tools.lang.trace

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class TraceInputStream(
    private val delegate: InputStream,
    private val byteArrayOutputStream: ByteArrayOutputStream
) : InputStream() {

    private var byteArrayInputStream: ByteArrayInputStream? = null

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
        byteArrayInputStream?.reset()
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
        return if (byteArrayInputStream == null) {
            val ch = delegate.read()
            if (ch != -1) {
                byteArrayOutputStream.write(ch)
            } else {
                val byteArray = byteArrayOutputStream.toByteArray()
                byteArrayInputStream = ByteArrayInputStream(byteArray)
                byteArrayInputStream!!.skip(byteArray.size.toLong())
            }
            ch
        } else {
            byteArrayInputStream!!.read()
        }
    }
}