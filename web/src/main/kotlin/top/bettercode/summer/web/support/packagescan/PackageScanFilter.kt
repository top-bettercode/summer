package top.bettercode.summer.web.support.packagescan

/**
 * Filter that can be used with the [PackageScanClassResolver] resolver.
 */
fun interface PackageScanFilter {
    /**
     * Does the given class match
     *
     * @param type the class
     * @return true to include this class, false to skip it.
     */
    fun matches(type: Class<*>): Boolean
}
