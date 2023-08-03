package top.bettercode.summer.test.web.support

import java.util.*

class EmbeddedIdBean {
    var name: String? = null
    var intCode: Int? = null
    var price: Long? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is EmbeddedIdBean) {
            return false
        }
        return name == other.name && intCode == other.intCode && price == other.price
    }

    override fun hashCode(): Int {
        return Objects.hash(name, intCode, price)
    }

    override fun toString(): String {
        return "EmbeddedIdBean{" +
                "name='" + name + '\'' +
                ", intCode=" + intCode +
                ", price=" + price +
                '}'
    }
}