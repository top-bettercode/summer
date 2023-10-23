package top.bettercode.summer.data.jpa.domain

import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.Type
import top.bettercode.summer.data.jpa.LogicalDelete
import top.bettercode.summer.tools.lang.util.StringUtil.json
import java.util.*
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@Suppress("LeakingThis")
@MappedSuperclass
open class BaseUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    open var id: Int? = null
    open var firstName: String? = null
    open var lastName: String? = null

    @LogicalDelete
    @Type(type = "org.hibernate.type.NumericBooleanType")
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