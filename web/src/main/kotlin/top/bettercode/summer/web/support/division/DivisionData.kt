package top.bettercode.summer.web.support.division

import top.bettercode.summer.tools.lang.property.Settings

/**
 *
 * @author Peter Wu
 */
object DivisionData {

    const val VNODE_NAME = "市辖区"

    /**
     * 虚拟层级
     */
    @JvmStatic
    fun isVnode(cityName: String?): Boolean {
        return VNODE_NAME == cityName
    }

    @JvmStatic
    val provinces: List<Division> by lazy {
        val codes = Settings.division.all()
        codes.filter { it.key.endsWith("0000") }.map { (provinceCode, provinceName) ->
            val citys = codes.filter {
                it.key.startsWith(
                    provinceCode.substring(
                        0,
                        2
                    )
                ) && it.key.endsWith("00") && it.key != provinceCode
            }
            val municipality = citys.size == 1 && citys.any { isVnode(it.value) }

            val cityDivisions = citys.map { (cityCode, cityName) ->
                val vnode = isVnode(cityName)
                val counties = codes
                    .filter { it.key.startsWith(cityCode.subSequence(0, 4)) && it.key != cityCode }
                    .map { (countyCode, countyName) ->
                        val division =
                            Division(
                                code = countyCode,
                                name = countyName,
                                level = 3,
                                municipality = false,
                                vnode = false,
                                parentNames = if (vnode) listOf(provinceName) else listOf(
                                    provinceName,
                                    cityName
                                ),
                                children = emptyList()
                            )
                        divisions[countyCode] = division
                        if (!vnode)
                            divisionNames[listOf(provinceName, cityName, countyName)] =
                                division
                        else {
                            divisionNames[listOf(provinceName, countyName)] = division
                        }
                        division
                    }.sortedBy { it.code }
                val division =
                    Division(
                        code = cityCode,
                        name = cityName,
                        level = 2,
                        municipality = false,
                        vnode = vnode,
                        parentNames = listOf(provinceName),
                        children = counties
                    )
                divisions[cityCode] = division
                if (!vnode)
                    divisionNames[listOf(provinceName, cityName)] = division
                division
            }.sortedBy { it.code }
            val division = Division(
                code = provinceCode,
                name = provinceName,
                level = 1,
                municipality = municipality,
                vnode = false,
                parentNames = emptyList(),
                children = cityDivisions
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