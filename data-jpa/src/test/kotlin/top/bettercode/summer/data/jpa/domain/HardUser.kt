package top.bettercode.summer.data.jpa.domain

import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.Type
import top.bettercode.summer.tools.lang.util.StringUtil.json
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "t_user")
class HardUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int? = null
    var firstName: String? = null
    var lastName: String? = null

    @Type(type = "org.hibernate.type.NumericBooleanType")
    @ColumnDefault("0")
    var deleted: Boolean? = null

    constructor()
    constructor(firstName: String?, lastName: String?) {
        this.firstName = firstName
        this.lastName = lastName
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is HardUser) {
            return false
        }
        val hardUser = o
        return deleted === hardUser.deleted && id == hardUser.id && firstName == hardUser.firstName && lastName == hardUser.lastName
    }

    override fun hashCode(): Int {
        return Objects.hash(id, firstName, lastName, deleted)
    }

    override fun toString(): String {
        return json(this)
    }
}