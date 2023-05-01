package top.bettercode.summer.test.web.support

import java.util.*

class EmbeddedIdBean {
    var name: String? = null
    var intCode: Int? = null
    var price: Long? = null
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is EmbeddedIdBean) {
            return false
        }
        val that = o
        return name == that.name && intCode == that.intCode && price == that.price
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