package ch.qos.logback.core.rolling

import ch.qos.logback.core.joran.spi.NoAutoStart
import ch.qos.logback.core.util.FileSize
import java.io.File

@NoAutoStart
class StartAndSizeAndTimeBasedRollingPolicy<E> : TimeBasedRollingPolicy<E>() {

    var maxFileSize: FileSize? = null

    override fun start() {
        val sizeAndTimeBasedFNATP = SizeAndTimeBasedFNATP<E>(SizeAndTimeBasedFNATP.Usage.EMBEDDED)
        if (maxFileSize == null) {
            addError("maxFileSize property is mandatory.")
            return
        } else {
            addInfo("Archive files will be limited to [$maxFileSize] each.")
        }
        sizeAndTimeBasedFNATP.setMaxFileSize(maxFileSize)
        timeBasedFileNamingAndTriggeringPolicy = sizeAndTimeBasedFNATP
        if (!isUnboundedTotalSizeCap && totalSizeCap.size < maxFileSize!!.size) {
            addError("totalSizeCap of [$totalSizeCap] is smaller than maxFileSize [$maxFileSize] which is non-sensical")
            return
        }
        // most work is done by the parent
        super.start()
        synchronized(this) {
            val parentsRawFileProperty = parentsRawFileProperty
            //启动时开启新日志
            if (File(parentsRawFileProperty).length() > 0) {
                sizeAndTimeBasedFNATP.atomicNextCheck.set(0L)
                isTriggeringEvent(null, null)
                try {
                    rollover()
                } catch (e: RolloverFailure) { //Do nothing
                    e.printStackTrace()
                }
            }
        }
    }


    override fun toString(): String {
        return "c.q.l.core.rolling.StartAndSizeAndTimeBasedRollingPolicy@" + this.hashCode()
    }

}