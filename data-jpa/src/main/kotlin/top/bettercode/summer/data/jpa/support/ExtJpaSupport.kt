package top.bettercode.summer.data.jpa.support

/**
 * @author Peter Wu
 */
interface ExtJpaSupport {
    fun setSoftDeleted(entity: Any)
    fun setUnSoftDeleted(entity: Any)
    fun supportSoftDeleted(): Boolean
    fun isSoftDeleted(entity: Any): Boolean
    fun softDeletedSeted(entity: Any): Boolean
    val softDeletedPropertyType: Class<*>?
    val softDeletedPropertyName: String?
    val lastModifiedDatePropertyName: String?
    val versionPropertyName: String?
    val lastModifiedDateNowValue: Any?
    val versionIncValue: Any?
    val softDeletedTrueValue: Any?
    val softDeletedFalseValue: Any?
}
