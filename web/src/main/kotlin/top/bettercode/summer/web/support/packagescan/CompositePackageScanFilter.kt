package top.bettercode.summer.web.support.packagescan

/**
 * `CompositePackageScanFilter` allows multiple [PackageScanFilter]s to be composed into
 * a single filter. For a [Class] to match a [CompositePackageScanFilter] it must match
 * each of the filters the composite contains
 */
class CompositePackageScanFilter : PackageScanFilter {
    private val filters: MutableSet<PackageScanFilter>

    constructor() {
        filters = LinkedHashSet()
    }

    constructor(filters: Set<PackageScanFilter>) {
        this.filters = LinkedHashSet(filters)
    }

    fun addFilter(filter: PackageScanFilter) {
        filters.add(filter)
    }

    override fun matches(type: Class<*>): Boolean {
        for (filter in filters) {
            if (!filter.matches(type)) {
                return false
            }
        }
        return true
    }
}
