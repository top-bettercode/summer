package top.bettercode.summer.data.jpa.domain

import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "t_user")
class StaticUser : BaseUser {
    constructor()
    constructor(firstName: String?, lastName: String?) : super(firstName, lastName)
}