package top.bettercode.summer.tools.excel

import top.bettercode.summer.tools.lang.util.RandomUtil.nextInt
import java.math.BigDecimal
import java.util.*

class DataBean {
    var intCode: Int
    var integer: Int
    var longl: Long
    var doublel: Double

    var floatl: Float

    var name: String
    var num: BigDecimal
    var date: Date

    constructor() {
        intCode = 1
        integer = 1
        longl = Date().time
        doublel = 4.4
        floatl = 5.5f
        num = BigDecimal("0." + nextInt(2))
        name = "名称"
        date = Date()
    }

    constructor(index: Int) {
        intCode = 1 + index / 3
        integer = 1 + index / 2
        longl = Date().time + index * 10000
        doublel = 4.4 + index
        floatl = 5.5f + index
        num = BigDecimal("0.$index")
        name = "名称$index"
        date = Date()
    }

}
