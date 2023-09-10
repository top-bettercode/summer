package top.bettercode.summer.test.web

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import top.bettercode.summer.web.resolver.UnitConveter
import top.bettercode.summer.web.serializer.annotation.JsonArray
import top.bettercode.summer.web.serializer.annotation.JsonBigDecimal
import top.bettercode.summer.web.serializer.annotation.JsonDefault
import top.bettercode.summer.web.serializer.annotation.JsonUrl
import java.math.BigDecimal
import java.util.*
import javax.validation.constraints.NotNull

@JsonIgnoreProperties(ignoreUnknown = true)
class StringArrayBean {
    @JsonDefault("0")
    var number1: @NotNull BigDecimal? = null

    @JsonBigDecimal(scale = 3)
    var number2: BigDecimal? = null

    @JsonBigDecimal(scale = 3, reduceFraction = true)
    var number22: BigDecimal? = null

    @JsonBigDecimal(scale = 4, toPlainString = true, percent = true, reduceFraction = true)
    var number3: BigDecimal? = null

    @JsonBigDecimal(scale = 4, toPlainString = true, reduceFraction = true)
    var number4: BigDecimal? = null
    var name: String? = null
    var code: String? = null
    var intCode: Int? = null

    @UnitConveter
    var price: Long? = null

    @JsonUrl
    var path: String? = null
    var path1: String? = null
    var desc: String? = null

    @JsonArray
    var ary: String? = null
        private set
    var paths1: List<String>? = null
    var pathArray1: Array<String>? = null

    fun setAry(ary: String?): StringArrayBean {
        this.ary = ary
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is StringArrayBean) {
            return false
        }
        return number1 == other.number1 && number2 == other.number2 && number22 == other.number22 && number3 == other.number3 && number4 == other.number4 && name == other.name && code == other.code && intCode == other.intCode && price == other.price && path == other.path && path1 == other.path1 && desc == other.desc && paths1 == other.paths1 && pathArray1.contentEquals(other.pathArray1)
    }

    override fun hashCode(): Int {
        var result = Objects.hash(number1, number2, number22, number3, number4, name, code, intCode,
                price, path, path1, desc, paths1)
        result = 31 * result + pathArray1.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "DataDicBean{" +
                "number1=" + number1 +
                ", number2=" + number2 +
                ", number22=" + number22 +
                ", number3=" + number3 +
                ", number4=" + number4 +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", intCode=" + intCode +
                ", price=" + price +
                ", path='" + path + '\'' +
                ", path1='" + path1 + '\'' +
                ", desc='" + desc + '\'' +
                ", paths=" + paths1 +
                ", pathArray=" + pathArray1?.contentToString() +
                '}'
    }
}