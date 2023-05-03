package top.bettercode.summer.data.jpa.resp

import java.util.*

class LastName {
    var lastName: String? = null
    var isDeleted = false
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is LastName) {
            return false
        }
        val lastName1 = o
        return isDeleted == lastName1.isDeleted && lastName == lastName1.lastName
    }

    override fun hashCode(): Int {
        return Objects.hash(lastName, isDeleted)
    }
}
