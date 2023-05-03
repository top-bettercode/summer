package top.bettercode.summer.data.jpa.domain

import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "t_user")
class StaticUser : BaseUser {
    constructor()
    constructor(firstName: String?, lastName: String?) : super(firstName, lastName)
}