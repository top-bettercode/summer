package top.bettercode.summer.tools.lang.trace

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream

class TraceServletInputStream(
        private val delegate: ServletInputStream,
        private val byteArrayOutputStream: ByteArrayOutputStream
) : ServletInputStream() {

    private var byteArrayInputStream: ByteArrayInputStream? = null

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
        if (byteArrayInputStream != null) {
            byteArrayInputStream!!.reset()
        } else {
            delegate.reset()
        }
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


    override fun setReadListener(listener: ReadListener?) {
        delegate.setReadListener(listener)
    }
}