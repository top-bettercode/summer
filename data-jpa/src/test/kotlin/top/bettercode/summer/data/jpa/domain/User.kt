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
class User : BaseUser {


    /**
     * 修改人
     */
    @LastModifiedBy
    var lastModifiedBy: String? = null
        private set

    /**
     * 修改时间 默认值：CURRENT_TIMESTAMP
     */
    @LastModifiedDate
    var lastModifiedDate: LocalDateTime? = null
        private set

    /**
     * 创建时间 默认值：CURRENT_TIMESTAMP
     */
    @CreatedDate
    var createdDate: LocalDateTime? = null
        private set

    @Version
    @ColumnDefault("0")
    var version: Int? = null
        private set

    constructor()
    constructor(firstName: String?, lastName: String?) : super(firstName, lastName)

    fun setLastModifiedDate(lastModifiedDate: LocalDateTime?): User {
        this.lastModifiedDate = lastModifiedDate
        return this
    }

    fun setCreatedDate(createdDate: LocalDateTime?): User {
        this.createdDate = createdDate
        return this
    }

    fun setVersion(version: Int?): User {
        this.version = version
        return this
    }
}