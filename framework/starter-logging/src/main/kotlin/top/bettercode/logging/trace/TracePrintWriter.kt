package top.bettercode.logging.trace

import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.util.*

class TracePrintWriter(private val delegate: PrintWriter, byteArrayOutputStream: ByteArrayOutputStream) : PrintWriter(delegate) {
    private val trace: PrintWriter = PrintWriter(byteArrayOutputStream, true)


    override fun write(c: Int) {
        delegate.write(c)
        trace.write(c)
        trace.flush()
    }

    override fun write(buf: CharArray, off: Int, len: Int) {
        delegate.write(buf, off, len)
        trace.write(buf, off, len)
        trace.flush()
    }


    override fun write(s: String, off: Int, len: Int) {
        delegate.write(s, off, len)
        trace.write(s, off, len)
        trace.flush()
    }

    override fun println() {
        delegate.println()
        trace.println()
        trace.flush()
    }

    override fun flush() {
        delegate.flush()
        trace.flush()
    }

    override fun checkError(): Boolean {
        trace.checkError()
        return delegate.checkError()
    }


    override fun format(format: String, vararg args: Any?): PrintWriter {
        trace.format(format, *args)
        trace.flush()
        return delegate.format(format, *args)
    }

    override fun format(l: Locale?, format: String, vararg args: Any?): PrintWriter {
        trace.format(l, format, *args)
        trace.flush()
        return delegate.format(l, format, *args)
    }


    override fun close() {
        delegate.close()
        trace.close()
    }

    override fun equals(other: Any?): Boolean {
        return delegate == other
    }

    override fun hashCode(): Int {
        return delegate.hashCode()
    }

    override fun toString(): String {
        return delegate.toString()
    }

}