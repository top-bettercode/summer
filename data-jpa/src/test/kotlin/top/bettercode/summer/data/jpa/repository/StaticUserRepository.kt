package top.bettercode.summer.data.jpa.repository

import top.bettercode.summer.data.jpa.JpaExtRepository
import top.bettercode.summer.data.jpa.domain.StaticUser

interface StaticUserRepository : JpaExtRepository<StaticUser?, Int?> {
    fun findByLastName(lastName: String?): List<StaticUser?>?
}