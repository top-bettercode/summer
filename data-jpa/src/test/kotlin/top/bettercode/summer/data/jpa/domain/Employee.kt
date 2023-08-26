package top.bettercode.summer.data.jpa.domain

import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.Type
import top.bettercode.summer.data.jpa.LogicalDelete
import top.bettercode.summer.tools.lang.util.StringUtil.json
import javax.persistence.EmbeddedId
import javax.persistence.Entity

@DynamicUpdate
@Entity
class Employee {
    @EmbeddedId
    var employeeKey: EmployeeKey? = null
    var firstName: String? = null
    var lastName: String? = null

    @LogicalDelete
    @Type(type = "org.hibernate.type.NumericBooleanType")
    @ColumnDefault("0")
    var deleted: Boolean? = null

    constructor()
    constructor(employeeKey: EmployeeKey?, firstName: String?, lastName: String?) {
        this.employeeKey = employeeKey
        this.firstName = firstName
        this.lastName = lastName
    }

    override fun toString(): String {
        return json(this)
    }
}