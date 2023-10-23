package top.bettercode.summer.data.jpa.domain

import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.Type
import top.bettercode.summer.data.jpa.LogicalDelete
import top.bettercode.summer.tools.lang.util.StringUtil.json
import javax.persistence.EmbeddedId
import javax.persistence.Entity

@Suppress("LeakingThis")
@DynamicUpdate
@Entity
open class Employee {
    @EmbeddedId
    open var employeeKey: EmployeeKey? = null
    open var firstName: String? = null
    open var lastName: String? = null

    @LogicalDelete
    @Type(type = "org.hibernate.type.NumericBooleanType")
    @ColumnDefault("0")
    open var deleted: Boolean? = null

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