package top.bettercode.summer.data.jpa.resp

import top.bettercode.summer.tools.lang.util.StringUtil.json
import java.util.*

/**
 * @author Peter Wu
 */
class CUser {
    var firstName: String? = null
    var lastName: List<String>? = null
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is CUser) {
            return false
        }
        val cUser = o
        return firstName == cUser.firstName && lastName == cUser.lastName
    }

    override fun hashCode(): Int {
        return Objects.hash(firstName, lastName)
    }

    override fun toString(): String {
        return json(this)
    }
}
