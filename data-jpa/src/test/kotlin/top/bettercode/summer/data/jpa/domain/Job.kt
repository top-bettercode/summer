package top.bettercode.summer.data.jpa.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.JdbcType
import org.hibernate.type.descriptor.jdbc.TinyIntJdbcType
import top.bettercode.summer.data.jpa.LogicalDelete
import top.bettercode.summer.tools.lang.util.StringUtil.json

@DynamicUpdate
@Entity
open class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "jobid")
    @GenericGenerator(name = "jobid", strategy = "uuid2")
    open var id: String? = null
    open var name: String? = null

    @LogicalDelete
    @JdbcType(TinyIntJdbcType::class)
    @ColumnDefault("0")
    open var deleted: Boolean? = null
    override fun toString(): String {
        return json(this)
    }
}