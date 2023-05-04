package top.bettercode.summer.data.jpa.domain

import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.Type
import top.bettercode.summer.data.jpa.SoftDelete
import top.bettercode.summer.tools.lang.util.StringUtil.json
import java.util.*
import javax.persistence.*

@MappedSuperclass
open class BaseUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int? = null
    var firstName: String? = null
    var lastName: String? = null

    @SoftDelete
    @Type(type = "org.hibernate.type.NumericBooleanType")
    @ColumnDefault("0")
    var deleted: Boolean? = null

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
        val baseUser = other
        return id == baseUser.id && firstName == baseUser.firstName && lastName == baseUser.lastName && deleted == baseUser.deleted
    }

    override fun hashCode(): Int {
        return Objects.hash(id, firstName, lastName, deleted)
    }

    override fun toString(): String {
        return json(this)
    }
}