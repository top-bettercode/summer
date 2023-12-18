package top.bettercode.summer.data.jpa.domain

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.JdbcType
import org.hibernate.type.descriptor.jdbc.TinyIntJdbcType
import top.bettercode.summer.data.jpa.LogicalDelete
import top.bettercode.summer.tools.lang.util.StringUtil.json

@Suppress("LeakingThis")
@DynamicUpdate
@Entity
open class Employee {
    @EmbeddedId
    open var employeeKey: EmployeeKey? = null
    open var firstName: String? = null
    open var lastName: String? = null

    @LogicalDelete
    @JdbcType(TinyIntJdbcType::class)
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