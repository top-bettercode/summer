package top.bettercode.summer.test.web

import top.bettercode.summer.web.resolver.Unit
import top.bettercode.summer.web.serializer.annotation.JsonBigDecimal
import top.bettercode.summer.web.serializer.annotation.JsonDefault
import top.bettercode.summer.web.serializer.annotation.JsonUrl
import java.math.BigDecimal
import java.util.*
import javax.validation.constraints.NotNull

open class DataDicBean {
    @JsonDefault("0")
    var number1: @NotNull BigDecimal? = null

    @JsonBigDecimal(scale = 3)
    var number2: BigDecimal? = null

    @JsonDefault(fieldName = "number2")
    @JsonBigDecimal(scale = 3, stripTrailingZeros = true)
    var number22: BigDecimal? = null

    @JsonBigDecimal(scale = 4, toPlainString = true, percent = true, stripTrailingZeros = true)
    var number3: BigDecimal? = null

    @JsonBigDecimal(scale = 4, toPlainString = true, stripTrailingZeros = true)
    var number4: BigDecimal? = null
    var name: String? = null
    var code: String? = null
    open var intCode: Int? = null

    @Unit
    var price: Long? = null

    @JsonUrl
    var path: String? = null
    var path1: String? = null
    var desc: String? = null
    var paths: MutableList<String>? = null
    var pathArray: Array<String>? = null
    val paths1: List<String?>?
        get() = paths

    open fun getPathArray1(): Array<String>? {
        return pathArray
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is DataDicBean) {
            return false
        }
        return number1 == other.number1 && number2 == other.number2 && number22 == other.number22 && number3 == other.number3 && number4 == other.number4 && name == other.name && code == other.code && intCode == other.intCode && price == other.price && path == other.path && path1 == other.path1 && desc == other.desc && paths == other.paths && pathArray.contentEquals(other.pathArray)
    }

    override fun hashCode(): Int {
        var result = Objects.hash(number1, number2, number22, number3, number4, name, code, intCode,
                price, path, path1, desc, paths)
        result = 31 * result + pathArray.contentHashCode()
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
                ", paths=" + paths +
                ", pathArray=" + pathArray.contentToString() +
                '}'
    }
}