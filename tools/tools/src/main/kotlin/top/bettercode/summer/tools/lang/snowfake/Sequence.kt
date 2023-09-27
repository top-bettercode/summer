package top.bettercode.summer.tools.lang.snowfake

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.ThreadLocalRandom

/**
 *
 *
 * 分布式高效有序ID生产黑科技(sequence) <br></br> 优化开源项目：[...](http://git.oschina.net/yu120/sequence)
 *
 *
 * @author hubin
 * @date 2016-08-18
 */
class Sequence {
    private val workerIdBits = 5L /* 机器标识位数 */
    private val datacenterIdBits = 5L
    private val maxWorkerId = (-1L shl workerIdBits.toInt()).inv()
    private val maxDatacenterId = (-1L shl datacenterIdBits.toInt()).inv()
    private val workerId: Long

    /* 数据标识id部分 */
    private val datacenterId: Long
    private var sequence = 0L /* 0，并发控制 */
    private var lastTimestamp = -1L /* 上次生产id时间戳 */
    private val lock = Object()

    constructor() {
        datacenterId = getDatacenterId(maxDatacenterId)
        workerId = getMaxWorkerId(datacenterId, maxWorkerId)
    }

    /**
     * @param workerId     工作机器ID
     * @param datacenterId 序列号
     */
    constructor(workerId: Long, datacenterId: Long) {
        check(!(workerId > maxWorkerId || workerId < 0)) { String.format("worker Id can't be greater than %d or less than 0", maxWorkerId) }
        check(!(datacenterId > maxDatacenterId || datacenterId < 0)) { String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId) }
        this.workerId = workerId
        this.datacenterId = datacenterId
    }

    /**
     * 获取下一个ID
     */
    @Synchronized
    fun nextId(): Long {
        var timestamp = timeGen()
        if (timestamp < lastTimestamp) { //闰秒
            val offset = lastTimestamp - timestamp
            if (offset <= 5) {
                try {
                    lock.wait(offset shl 1)
                    timestamp = timeGen()
                    if (timestamp < lastTimestamp) {
                        throw RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
                                offset))
                    }
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            } else {
                throw RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
                        offset))
            }
        }

        /* 毫秒内自增位 */
        val sequenceBits = 12L
        if (lastTimestamp == timestamp) {
            // 相同毫秒内，序列号自增
            val sequenceMask = (-1L shl sequenceBits.toInt()).inv()
            sequence = sequence + 1 and sequenceMask
            if (sequence == 0L) {
                // 同一毫秒的序列数已经达到最大
                timestamp = tilNextMillis(lastTimestamp)
            }
        } else {
            // 不同毫秒内，序列号置为 1 - 3 随机数
            sequence = ThreadLocalRandom.current().nextLong(1, 3)
        }
        lastTimestamp = timestamp

        /* 时间戳左移动位 */
        val timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits
        val datacenterIdShift = sequenceBits + workerIdBits
        /* 时间起始标记点，作为基准，一般取系统的最近时间（一旦确定不能变动） */
        val twepoch = 1288834974657L
        return (timestamp - twepoch shl timestampLeftShift.toInt() // 时间戳部分
                or (datacenterId shl datacenterIdShift.toInt()) // 数据中心部分
                or (workerId shl sequenceBits.toInt()) // 机器标识部分
                or sequence) // 序列号部分
    }

    private fun tilNextMillis(lastTimestamp: Long): Long {
        var timestamp = timeGen()
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen()
        }
        return timestamp
    }

    private fun timeGen(): Long {
        return SystemClock.now()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(Sequence::class.java)

        /**
         *
         *
         * 获取 maxWorkerId
         *
         */
        private fun getMaxWorkerId(datacenterId: Long, maxWorkerId: Long): Long {
            val mpid = StringBuilder()
            mpid.append(datacenterId)
            val name = ManagementFactory.getRuntimeMXBean().name
            if (!name.isNullOrBlank()) {
                /*
       * GET jvmPid
       */
                mpid.append(name.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
            }
            /*
     * MAC + PID 的 hashcode 获取16个低位
     */return (mpid.toString().hashCode() and 0xffff) % (maxWorkerId + 1)
        }

        /**
         *
         *
         * 数据标识id部分
         *
         */
        private fun getDatacenterId(maxDatacenterId: Long): Long {
            var id = 0L
            try {
                val ip = InetAddress.getLocalHost()
                val network = NetworkInterface.getByInetAddress(ip)
                if (network == null) {
                    id = 1L
                } else {
                    val mac = network.hardwareAddress
                    if (null != mac) {
                        id = 0x000000FFL and mac[mac.size - 1].toLong() or (0x0000FF00L and (mac[mac.size - 2].toLong() shl 8)) shr 6
                        id %= (maxDatacenterId + 1)
                    }
                }
            } catch (e: Exception) {
                log.warn(" getDatacenterId: " + e.message)
            }
            return id
        }
    }
}
