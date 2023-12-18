package top.bettercode.summer.data.jpa.domain

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.JdbcType
import org.hibernate.type.descriptor.jdbc.TinyIntJdbcType
import top.bettercode.summer.tools.lang.util.StringUtil.json
import java.util.*

@Suppress("LeakingThis")
@Entity
@DynamicUpdate
@Table(name = "t_user")
open class PhysicalUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    open var id: Int? = null
    open var firstName: String? = null
    open var lastName: String? = null

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
        if (other !is PhysicalUser) {
            return false
        }
        return deleted === other.deleted && id == other.id && firstName == other.firstName && lastName == other.lastName
    }

    override fun hashCode(): Int {
        return Objects.hash(id, firstName, lastName, deleted)
    }

    override fun toString(): String {
        return json(this)
    }
}