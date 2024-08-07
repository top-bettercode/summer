package top.bettercode.summer.tools.lang.log

import ch.qos.logback.classic.Level
import org.slf4j.Marker
import java.util.concurrent.CopyOnWriteArrayList

/**
 * A alarm implementation of the [Marker] interface.
 *
 */
class AlarmMarker(
    val message: String? = null,
    val timeout: Boolean = false,
    val level: Level = Level.WARN
) : Marker {
    private val referenceList: MutableList<Marker> = CopyOnWriteArrayList()
    override fun getName(): String {
        return AlarmAppender.ALARM_LOG_MARKER
    }

    override fun add(reference: Marker) {
        // no point in adding the reference multiple times
        if (this.contains(reference)) {
            return
        } else if (reference.contains(this)) { // avoid recursion
            // a potential reference should not hold its future "parent" as a reference
            return
        } else {
            referenceList.add(reference)
        }
    }

    override fun hasReferences(): Boolean {
        return referenceList.size > 0
    }

    @Deprecated("Deprecated in Java", ReplaceWith("hasReferences()"))
    override fun hasChildren(): Boolean {
        return hasReferences()
    }

    override fun iterator(): Iterator<Marker> {
        return referenceList.iterator()
    }

    override fun remove(referenceToRemove: Marker): Boolean {
        return referenceList.remove(referenceToRemove)
    }

    override fun contains(other: Marker): Boolean {
        if (this == other) {
            return true
        }
        if (hasReferences()) {
            for (ref in referenceList) {
                if (ref.contains(other)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * This method is mainly used with Expression Evaluators.
     */
    override fun contains(name: String): Boolean {
        if (this.name == name) {
            return true
        }
        if (hasReferences()) {
            for (ref in referenceList) {
                if (ref.contains(name)) {
                    return true
                }
            }
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other !is Marker) {
            return false
        }
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        if (!hasReferences()) {
            return getName()
        }
        val it = this.iterator()
        var reference: Marker
        val sb = StringBuilder(getName())
        sb.append(' ').append(OPEN)
        while (it.hasNext()) {
            reference = it.next()
            sb.append(reference.name)
            if (it.hasNext()) {
                sb.append(SEP)
            }
        }
        sb.append(CLOSE)
        return sb.toString()
    }

    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID = -2849567615646933777L
        private const val OPEN = "[ "
        private const val CLOSE = " ]"
        private const val SEP = ", "
    }
}
