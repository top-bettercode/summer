package top.bettercode.summer.data.jpa.domain

import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import top.bettercode.summer.data.jpa.SoftDelete
import top.bettercode.summer.tools.lang.util.StringUtil.json
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@DynamicUpdate
@Entity
class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "jobid")
    @GenericGenerator(name = "jobid", strategy = "uuid2")
    var id: String? = null
    var name: String? = null

    @SoftDelete
    @Type(type = "org.hibernate.type.NumericBooleanType")
    @ColumnDefault("0")
    var deleted: Boolean? = null
    override fun toString(): String {
        return json(this)
    }
}