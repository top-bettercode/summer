package top.bettercode.summer.data.jpa.domain

import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.DynamicUpdate
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.Table
import javax.persistence.Version

@DynamicUpdate
@Entity
@Table(name = "t_user")
@EntityListeners(AuditingEntityListener::class)
open class User : BaseUser {


    /**
     * 修改人
     */
    @LastModifiedBy
    open var lastModifiedBy: String? = null

    /**
     * 修改时间 默认值：CURRENT_TIMESTAMP
     */
    @LastModifiedDate
    open var lastModifiedDate: LocalDateTime? = null

    /**
     * 创建时间 默认值：CURRENT_TIMESTAMP
     */
    @CreatedDate
    open var createdDate: LocalDateTime? = null

    @Version
    @ColumnDefault("0")
    open var version: Int? = null

    constructor()
    constructor(firstName: String?, lastName: String?) : super(firstName, lastName)

}