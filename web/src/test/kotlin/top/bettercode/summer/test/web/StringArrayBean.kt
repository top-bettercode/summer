package top.bettercode.summer.test.web

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import top.bettercode.summer.web.resolver.Cent
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

    @Cent
    var price: Long? = null

    @JsonUrl
    var path: String? = null
    var path1: String? = null
    var desc: String? = null

    @JsonArray
    var ary: String? = null
        private set
    var paths1: List<String>? = null
        get() = field
        set
    var pathArray1: Array<String>? = null

    fun setAry(ary: String?): top.bettercode.summer.test.web.StringArrayBean {
        this.ary = ary
        return this
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is top.bettercode.summer.test.web.StringArrayBean) {
            return false
        }
        val that = o
        return number1 == that.number1 && number2 == that.number2 && number22 == that.number22 && number3 == that.number3 && number4 == that.number4 && name == that.name && code == that.code && intCode == that.intCode && price == that.price && path == that.path && path1 == that.path1 && desc == that.desc && paths1 == that.paths1 && Arrays.equals(pathArray1, that.pathArray1)
    }

    override fun hashCode(): Int {
        var result = Objects.hash(number1, number2, number22, number3, number4, name, code, intCode,
                price, path, path1, desc, paths1)
        result = 31 * result + Arrays.hashCode(pathArray1)
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
                ", pathArray=" + Arrays.toString(pathArray1) +
                '}'
    }
}