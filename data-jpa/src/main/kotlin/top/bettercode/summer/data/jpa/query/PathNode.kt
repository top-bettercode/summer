package top.bettercode.summer.data.jpa.query

import org.springframework.lang.Nullable
import org.springframework.util.ObjectUtils

class PathNode(val name: String, @field:Nullable @param:Nullable val parent: PathNode?, @field:Nullable @param:Nullable val value: Any?) {
    val siblings: MutableList<PathNode> = ArrayList()
    fun add(attribute: String, @Nullable value: Any?): PathNode {
        val node = PathNode(attribute, this, value)
        siblings.add(node)
        return node
    }

    fun spansCycle(): Boolean {
        if (value == null) {
            return false
        }
        val identityHex = ObjectUtils.getIdentityHexString(value)
        var current = parent
        while (current != null) {
            if (current.value != null && (ObjectUtils.getIdentityHexString(current.value!!)
                            == identityHex)) {
                return true
            }
            current = current.parent
        }
        return false
    }

    override fun toString(): String {
        val sb = StringBuilder()
        if (parent != null) {
            sb.append(parent)
            sb.append(" -")
            sb.append(name)
            sb.append("-> ")
        }
        sb.append("[{ ")
        sb.append(ObjectUtils.nullSafeToString(value))
        sb.append(" }]")
        return sb.toString()
    }
}
