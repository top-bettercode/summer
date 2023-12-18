package top.bettercode.summer.data.jpa.domain

import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.JdbcType
import org.hibernate.type.descriptor.jdbc.TinyIntJdbcType
import org.hibernate.annotations.DynamicUpdate
import top.bettercode.summer.data.jpa.LogicalDelete
import top.bettercode.summer.tools.lang.util.StringUtil.json
import java.util.*

@DynamicUpdate
@Suppress("LeakingThis")
@MappedSuperclass
open class BaseUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    open var id: Int? = null
    open var firstName: String? = null
    open var lastName: String? = null

    @LogicalDelete
    @JdbcType(TinyIntJdbcType::class)
    @ColumnDefault("0")
    open var deleted: Boolean? = null

    constructor()
    constructor(firstName: String?, lastName: String?) {
        this.firstName = firstName
        this.lastName = lastName
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is BaseUser) {
            return false
        }
        return id == other.id && firstName == other.firstName && lastName == other.lastName && deleted == other.deleted
    }

    override fun hashCode(): Int {
        return Objects.hash(id, firstName, lastName, deleted)
    }

    override fun toString(): String {
        return json(this)
    }
}