package top.bettercode.summer.data.jpa.support

/**
 * @author Peter Wu
 */
interface ExtJpaSupport {
    fun setLogicalDeleted(entity: Any)
    fun setUnLogicalDeleted(entity: Any)
    fun supportLogicalDeleted(): Boolean
    fun isLogicalDeleted(entity: Any): Boolean
    fun logicalDeletedSeted(entity: Any): Boolean

    fun lastModifiedBy(auditor: Any?): Any?

    val logicalDeletedPropertyType: Class<*>?
    val logicalDeletedPropertyName: String?
    val lastModifiedDatePropertyName: String?
    val lastModifiedByPropertyName: String?
    val versionPropertyName: String?
    val lastModifiedDateNowValue: Any?
    val versionIncValue: Any?
    val logicalDeletedTrueValue: Any?
    val logicalDeletedFalseValue: Any?
}
