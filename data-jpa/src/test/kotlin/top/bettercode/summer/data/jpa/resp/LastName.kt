package top.bettercode.summer.data.jpa.resp

import java.util.*

class LastName {
    var lastName: String? = null
    var isDeleted = false
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is LastName) {
            return false
        }
        val lastName1 = other
        return isDeleted == lastName1.isDeleted && lastName == lastName1.lastName
    }

    override fun hashCode(): Int {
        return Objects.hash(lastName, isDeleted)
    }
}
