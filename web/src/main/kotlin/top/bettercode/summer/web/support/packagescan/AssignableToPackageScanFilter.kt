package top.bettercode.summer.web.support.packagescan

/**
 * Package scan filter for testing if a given class is assignable to another class.
 */
class AssignableToPackageScanFilter : PackageScanFilter {
    private val parents: MutableSet<Class<*>> = HashSet()

    constructor()
    constructor(parentType: Class<*>) {
        parents.add(parentType)
    }

    constructor(parents: Set<Class<*>>?) {
        this.parents.addAll(parents!!)
    }

    fun addParentType(parentType: Class<*>) {
        parents.add(parentType)
    }

    override fun matches(type: Class<*>): Boolean {
        if (parents.size > 0) {
            for (parent in parents) {
                if (parent.isAssignableFrom(type) && parent != type) {
                    return true
                }
            }
        }
        return false
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (parent in parents) {
            sb.append(parent.simpleName).append(", ")
        }
        sb.setLength(if (sb.length > 0) sb.length - 2 else 0)
        return "is assignable to $sb"
    }
}
