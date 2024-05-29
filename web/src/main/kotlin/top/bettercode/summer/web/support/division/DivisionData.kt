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
    fun isVnode(prefectureName: String?): Boolean {
        return VNODE_NAME == prefectureName
    }

    @JvmStatic
    val provinces: List<Division> by lazy {
        val codes = Settings.areaCode.all()
        codes.filter { it.key.endsWith("0000") }.map { (provinceCode, provinceName) ->
            var prefectures = codes.filter { it.key.startsWith(provinceCode.substring(0, 2)) && it.key.endsWith("00") && it.key != provinceCode }
            val municipality = if (prefectures.isEmpty() && codes.any { it.key.startsWith(provinceCode.trimEnd('0')) && it.key != provinceCode }) {
                prefectures = mapOf(provinceCode.substring(0, 2) + "0100" to VNODE_NAME)
                true
            } else false

            val prefectureDivisions = prefectures.map { (prefectureCode, prefectureName) ->
                val vnode = isVnode(prefectureName)
                val counties = codes
                        .filter { it.key.startsWith(prefectureCode.subSequence(0, 4)) && it.key != prefectureCode }
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
                                                    prefectureName
                                            ),
                                            children = emptyList()
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
                                code = prefectureCode,
                                name = prefectureName,
                                level = 2,
                                municipality = false,
                                vnode = vnode,
                                parentNames = listOf(provinceName),
                                children = counties
                        )
                divisions[prefectureCode] = division
                if (!vnode)
                    divisionNames[listOf(provinceName, prefectureName)] = division
                division
            }.sortedBy { it.code }
            val division = Division(
                    code = provinceCode,
                    name = provinceName,
                    level = 1,
                    municipality = municipality,
                    vnode = false,
                    parentNames = emptyList(),
                    children = prefectureDivisions
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