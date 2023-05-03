package top.bettercode.summer.data.jpa.domain

import top.bettercode.summer.tools.lang.util.StringUtil.json
import java.io.Serializable
import java.util.*
import javax.persistence.Embeddable

/**
 * 客商档案 主键 对应表名：BD_CVDOC
 */
@Embeddable
class EmployeeKey : Serializable {
    var id: Int? = null
    var id2: Int? = null

    constructor()
    constructor(id: Int?, id2: Int?) {
        this.id = id
        this.id2 = id2
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is EmployeeKey) {
            return false
        }
        val that = o
        return id == that.id && id2 == that.id2
    }

    override fun hashCode(): Int {
        return Objects.hash(id, id2)
    }

    override fun toString(): String {
        return json(this)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}