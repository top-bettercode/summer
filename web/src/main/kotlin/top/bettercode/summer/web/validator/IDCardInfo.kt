package top.bettercode.summer.web.validator

import top.bettercode.summer.tools.lang.property.Settings.areaCode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class IDCardInfo(var idcard: String) {
    // 省份
    var province: String? = null

    // 城市
    var city: String? = null

    // 区县
    var region: String? = null

    // 年份
    var year = 0

    // 月份
    var month = 0

    // 日期
    var day = 0

    // 性别
    var gender: String? = null

    // 出生日期
    var birthday: LocalDate? = null
        private set
    var isLegal = false
    private fun getString(key: String, defaultVal: String?): String {
        return areaCodes.getOrDefault(key, defaultVal ?: "")
    }

    init {
        if (IDCardUtil.validate(idcard)) {
            isLegal = true
            if (idcard.length == 15) {
                idcard = IDCardUtil.convertFrom15bit(idcard) ?: throw RuntimeException("身份证号码不正确")
            }
            // 获取省份
            val provinceId = idcard.substring(0, 2)
            val cityId = idcard.substring(2, 4)
            val regionId = idcard.substring(4, 6)
            province = getString(provinceId + "0000", null)
            city = getString(provinceId + cityId + "00", null)
            region = getString(provinceId + cityId + regionId, null)

            // 获取性别
            val id17 = idcard.substring(16, 17)
            gender = if (id17.toInt() % 2 != 0) {
                "男"
            } else {
                "女"
            }

            // 获取出生日期
            birthday = LocalDate.parse(idcard.substring(6, 14),
                    DateTimeFormatter.ofPattern("yyyyMMdd"))
            if (birthday != null) {
                year = birthday!!.year
                month = birthday!!.monthValue
                day = birthday!!.dayOfMonth
            }
        }
    }

    fun setBirthday(birthday: LocalDate?): IDCardInfo {
        this.birthday = birthday
        return this
    }

    override fun toString(): String {
        return if (isLegal) {
            ("出生地：" + province + city + region + ",生日：" + year + "年" + month + "月" + day
                    + "日,性别："
                    + gender)
        } else {
            "非法身份证号码"
        }
    }

    companion object {
        private val areaCodes = areaCode
    }
}
