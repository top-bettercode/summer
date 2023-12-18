package top.bettercode.summer.data.jpa.query.mybatis.hibernate

import org.hibernate.HibernateException
import org.hibernate.query.spi.ScrollableResultsImplementor

/**
 * @author Peter Wu
 */
class MybatisScrollableResultsImplementor<T>(private val list: List<T>?) : ScrollableResultsImplementor<T?> {
    private var closed = false
    private var currentPosition = 0
    private val maxPosition: Int = list!!.size

    override fun isClosed(): Boolean {
        return closed
    }

    override fun get(): T? {
        check(!closed) { "ScrollableResults is closed" }
        return if (currentPosition in 1..maxPosition) {
            list!![currentPosition - 1]
        } else {
            null
        }
    }

    override fun close() {
        if (closed) {
            // noop if already closed
            return
        }
        closed = true
    }

    override fun next(): Boolean {
        if (maxPosition == 0) {
            currentPosition = 0
            return false
        }
        if (maxPosition <= currentPosition) {
            currentPosition = maxPosition + 1
            return false
        }
        currentPosition++
        return true
    }

    override fun previous(): Boolean {
        if (currentPosition <= 1) {
            currentPosition = 0
            return false
        }
        currentPosition--
        return true
    }

    override fun scroll(positions: Int): Boolean {
        var more = false
        if (positions > 0) {
            // scroll ahead
            for (i in 0 until positions) {
                more = next()
                if (!more) {
                    break
                }
            }
        } else if (positions < 0) {
            // scroll backward
            for (i in 0 until -positions) {
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

    override fun position(position: Int): Boolean {
        if (position <= 0 || position > list!!.size) {
            return false
        }
        currentPosition = position - 1
        return next()
    }

    override fun last(): Boolean {
        var more = false
        if (currentPosition > maxPosition) {
            more = previous()
        }
        for (i in currentPosition until maxPosition) {
            more = next()
        }
        return more
    }

    override fun first(): Boolean {
        beforeFirst()
        return next()
    }

    override fun beforeFirst() {
        currentPosition = 0
    }

    override fun afterLast() {
        last()
        next()
    }

    override fun isFirst(): Boolean {
        return currentPosition == 1
    }

    override fun isLast(): Boolean {
        return currentPosition == maxPosition
    }

    override fun getRowNumber(): Int {
        return currentPosition
    }

    override fun setRowNumber(rowNumber: Int): Boolean {
        return when (rowNumber) {
            1 -> {
                first()
            }
            -1 -> {
                last()
            }
            maxPosition -> {
                last()
            }
            else -> scroll(rowNumber - currentPosition)
        }
    }

    override fun setFetchSize(fetchSize: Int) {}
}
