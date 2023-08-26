package top.bettercode.summer.tools.lang.snowfake

import java.sql.Timestamp
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 *
 *
 * 高并发场景下System.currentTimeMillis()的性能问题的优化
 *
 *
 *
 * System.currentTimeMillis()的调用比new一个普通对象要耗时的多（具体耗时高出多少我还没测试过，有人说是100倍左右）<br></br>
 * System.currentTimeMillis()之所以慢是因为去跟系统打了一次交道<br></br>
 * 后台定时更新时钟，JVM退出时，线程自动回收<br></br>
 * 10亿：43410,206,210.72815533980582%<br></br>
 * 1亿：4699,29,162.0344827586207%<br></br>
 * 1000万：480,12,40.0%<br></br>
 * 100万：50,10,5.0%<br></br>
 *
 *
 */
class SystemClock private constructor(private val period: Long) {
    private val now: AtomicLong = AtomicLong(System.currentTimeMillis())

    init {
        scheduleClockUpdating()
    }

    private fun scheduleClockUpdating() {
        val scheduler = Executors.newSingleThreadScheduledExecutor { runnable: Runnable? ->
            val thread = Thread(runnable, "System Clock")
            thread.setDaemon(true)
            thread
        }
        scheduler.scheduleAtFixedRate({ now.set(System.currentTimeMillis()) }, period, period, TimeUnit.MILLISECONDS)
    }

    private fun currentTimeMillis(): Long {
        return now.get()
    }

    private object InstanceHolder {
        val INSTANCE = SystemClock(1)
    }

    companion object {
        private fun instance(): SystemClock {
            return InstanceHolder.INSTANCE
        }

        fun now(): Long {
            return instance().currentTimeMillis()
        }

        fun nowDate(): String {
            return Timestamp(instance().currentTimeMillis()).toString()
        }
    }
}
