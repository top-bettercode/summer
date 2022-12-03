package top.bettercode.summer.tools.lang.trace

import java.io.ByteArrayOutputStream
import javax.servlet.ServletOutputStream
import javax.servlet.WriteListener

class TraceServletOutputStream(private val delegate: ServletOutputStream, private val byteArrayOutputStream: ByteArrayOutputStream) : ServletOutputStream() {

    private val trace = object : ServletOutputStream() {

        override fun isReady(): Boolean {
            return true
        }

        override fun setWriteListener(listener: WriteListener?) {
        }

        override fun write(b: Int) {
            byteArrayOutputStream.write(b)
        }

        override fun flush() {
            byteArrayOutputStream.flush()
        }

        override fun close() {
            byteArrayOutputStream.close()
        }

    }

    override fun isReady(): Boolean {
        return delegate.isReady
    }

    override fun setWriteListener(listener: WriteListener?) {
        this.delegate.setWriteListener(listener)
    }


    override fun write(b: Int) {
        this.delegate.write(b)
        trace.write(b)
    }

    override fun flush() {
        delegate.flush()
        trace.flush()
    }

    override fun close() {
        delegate.close()
        trace.close()
    }

    //--------------------------------------------
    override fun toString(): String {
        return delegate.toString()
    }
}