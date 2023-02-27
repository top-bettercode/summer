package top.bettercode.summer.web.support.gb2260

import top.bettercode.summer.tools.lang.property.Settings

/**
 *
 * @author Peter Wu
 */
object GB2260 {

    @JvmStatic
    val provinces: List<Division> by lazy {
        val codes = Settings.areaCode.all()
        codes.filter { it.key.endsWith("0000") }.map { (provinceCode, provinceName) ->
            var prefectures =
                codes
                    .filter {
                        it.key.startsWith(
                            provinceCode.substring(
                                0,
                                2
                            )
                        ) && it.key.endsWith("00") && it.key != provinceCode
                    }
            val municipality =
                if (prefectures.isEmpty() && codes.any { it.key.startsWith(provinceCode.trimEnd('0')) && it.key != provinceCode }) {
                    prefectures = mapOf(provinceCode.substring(0, 2) + "0100" to "市辖区")
                    true
                } else false

            val prefectureDivisions = prefectures.map { (prefectureCode, prefectureName) ->
                val vnode = provinceName == prefectureName
                val counties = codes
                    .filter { it.key.startsWith(prefectureCode.trimEnd('0')) && it.key != prefectureCode }
                    .map { (countyCode, countyName) ->
                        val division =
                            Division(
                                countyCode,
                                countyName,
                                3,
                                false,
                                if (vnode) listOf(provinceName) else listOf(
                                    provinceName,
                                    prefectureName
                                ),
                                emptyList()
                            )
                        divisions[countyCode] = division
                        if (!vnode)
                            divisionNames[listOf(provinceName, prefectureName, countyName)] =
                                division
                        else {
                            divisionNames[listOf(provinceName, countyName)] = division
                        }
                        division
                    }.sortedBy { it.code }
                val division =
                    Division(
                        prefectureCode,
                        prefectureName,
                        2,
                        false,
                        listOf(provinceName),
                        counties
                    )
                divisions[prefectureCode] = division
                if (!vnode)
                    divisionNames[listOf(provinceName, prefectureName)] = division
                division
            }.sortedBy { it.code }
            val division = Division(
                provinceCode,
                provinceName,
                1,
                municipality,
                emptyList(),
                prefectureDivisions
            )
            divisions[provinceCode] = division
            divisionNames[listOf(provinceName)] = division
            division
        }.sortedBy { it.code }
    }

    private val divisions: MutableMap<String, Division> = mutableMapOf()
    private val divisionNames: MutableMap<List<String>, Division> = mutableMapOf()

    @JvmStatic
    fun getDivision(code: String): Division {
        if (divisions.isEmpty()) provinces
        return divisions[code] ?: throw InvalidCodeException("无${code}对应行政区划")
    }

    @JvmStatic
    fun getDivisionByName(name: List<String>): Division {
        if (divisions.isEmpty()) provinces
        return divisionNames[name]
            ?: throw InvalidCodeException("无${name.joinToString()}对应行政区划")
    }

}