package top.bettercode.summer.data.jpa.resp

import top.bettercode.summer.tools.lang.util.StringUtil.json
import java.util.*

/**
 * @author Peter Wu
 */
class AUser {
    var id: Int? = null
    var firstName: String? = null
    var lastName: LastName? = null
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is AUser) {
            return false
        }
        val cUsers = o
        return firstName == cUsers.firstName && lastName == cUsers.lastName
    }

    override fun hashCode(): Int {
        return Objects.hash(firstName, lastName)
    }

    override fun toString(): String {
        return json(this)
    }
}
