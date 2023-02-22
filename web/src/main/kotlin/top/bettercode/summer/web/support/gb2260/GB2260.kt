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
        codes.filter { it.key.endsWith("0000") }.map { (code, name) ->
            var cities =
                codes
                    .filter {
                        it.key.startsWith(
                            code.substring(
                                0,
                                2
                            )
                        ) && it.key.endsWith("00") && it.key != code
                    }
            if (cities.isEmpty() && codes.any { it.key.startsWith(code.trimEnd('0')) && it.key != code }) {
                cities = mapOf(code.substring(0, 2) + "0100" to name)
            }
            val cityDivisions = cities.map { (cityCode, cityName) ->
                val vnode = name == cityName
                val counties = codes
                    .filter { it.key.startsWith(cityCode.trimEnd('0')) && it.key != cityCode }
                    .map { (countyCode, countyName) ->
                        val division =
                            Division(
                                countyCode,
                                countyName,
                                if (vnode) listOf(name) else listOf(name, cityName),
                                emptyList()
                            )
                        divisions[countyCode] = division
                        if (!vnode)
                            divisionNames[listOf(name, cityName, countyName)] = division
                        else {
                            divisionNames[listOf(name, countyName)] = division
                        }
                        division
                    }.sortedBy { it.code }
                val division = Division(cityCode, cityName, listOf(name), counties)
                divisions[cityCode] = division
                if (!vnode)
                    divisionNames[listOf(name, cityName)] = division
                division
            }.sortedBy { it.code }
            val division = Division(code, name, emptyList(), cityDivisions)
            divisions[code] = division
            divisionNames[listOf(name)] = division
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