package top.bettercode.logging.logback

import org.slf4j.Marker
import java.util.*

/**
 * @author Peter Wu
 */
class AlarmMarker(name: String, val timeoutMsg: String? = null) : Marker {
    private val name: String
    private var refereceList: MutableList<Marker>? = null
    override fun getName(): String {
        return name
    }

    @Synchronized
    override fun add(reference: Marker) {
        // no point in adding the reference multiple times
        if (this.contains(reference)) {
            return
        } else if (reference.contains(this)) { // avoid recursion
            // a potential reference should not its future "parent" as a reference
            return
        } else {
            // let's add the reference
            if (refereceList == null) {
                refereceList = Vector()
            }
            refereceList!!.add(reference)
        }
    }

    @Synchronized
    override fun hasReferences(): Boolean {
        return refereceList != null && refereceList!!.size > 0
    }

    override fun hasChildren(): Boolean {
        return hasReferences()
    }

    @Synchronized
    override fun iterator(): Iterator<Marker> {
        return if (refereceList != null) {
            refereceList!!.iterator()
        } else {
            Collections.emptyIterator()
        }
    }

    @Synchronized
    override fun remove(referenceToRemove: Marker): Boolean {
        if (refereceList == null) {
            return false
        }
        val size = refereceList!!.size
        for (i in 0 until size) {
            if (referenceToRemove == refereceList!![i]) {
                refereceList!!.removeAt(i)
                return true
            }
        }
        return false
    }

    override fun contains(other: Marker): Boolean {
        if (this == other) {
            return true
        }
        if (hasReferences()) {
            for (i in refereceList!!.indices) {
                if (refereceList!![i].contains(other)) {
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
            for (i in refereceList!!.indices) {
                if (refereceList!![i].contains(name)) {
                    return true
                }
            }
        }
        return false
    }

    /*
     * BEGIN Modification in logstash-logback-encoder to make this constructor public
     */
    init {
        /*
       * END Modification in logstash-logback-encoder to make this constructor public
       */
        this.name = name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other !is Marker) return false
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
        val sb = StringBuffer(getName())
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
        private const val serialVersionUID: Long = 1L
        private const val OPEN = "[ "
        private const val CLOSE = " ]"
        private const val SEP = ", "
    }
}