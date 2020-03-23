package ch.qos.logback.core.rolling

import ch.qos.logback.core.joran.spi.NoAutoStart
import ch.qos.logback.core.util.FileSize

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
        sizeAndTimeBasedFNATP.nextCheck = 0L
        isTriggeringEvent(null, null)
        try {
            rollover()
        } catch (e: RolloverFailure) { //Do nothing
        }
    }


    override fun toString(): String {
        return "c.q.l.core.rolling.SizeAndTimeBasedRollingPolicy@" + this.hashCode()
    }

}