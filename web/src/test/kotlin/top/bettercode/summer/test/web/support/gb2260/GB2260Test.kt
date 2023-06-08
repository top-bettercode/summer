package top.bettercode.summer.test.web.support.gb2260

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate
import top.bettercode.summer.tools.lang.property.PropertiesSource
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.web.support.gb2260.GB2260
import java.io.File

/**
 *
 * @author Peter Wu
 */
class GB2260Test {

    @Disabled
    @Test
    fun testReplenishLngLat() {
        val propertiesSource = PropertiesSource.of("area")
        val file = file()
        //new Lines
        val lines = mutableListOf<String>()
        file.readLines().forEachIndexed { index, s ->
            if (index == 0) {
                lines.add(s)
            } else {
                val split = s.split("=")
                val key = split[0]
                val value = split[1]
                val valext = propertiesSource[key]
                lines.add("$key=${value.trim()}${if (valext != null) ",$valext" else ""}")
            }
        }
        file.writeText(lines.joinToString("\n"))
    }

    private fun file() =
            File(GB2260Test::class.java.getResource("/areaCode.properties")?.toURI()?.toURL()?.file
                    ?: throw Exception("areaCode.properties not found"))

    @Disabled
    @Test
    fun testReplenishLngLat2() {
        val template = RestTemplate()
        val file = file()
        //new Lines
        val lines = mutableListOf<String>()
        file.readLines().forEachIndexed { index, s ->
            if (index == 0) {
                lines.add(s)
            } else {
                val split = s.split("=")
                val key = split[0]
                val value = split[1]
                //通过 RestTemplate 请求https://apis.map.qq.com/jsapi?qt=geoc&addr={0}&key=TU5BZ-MKD3W-L43RW-O3ZBW-GWMZK-QBB25&output=jsonp&pf=jsapi&ref=jsapi&cb=qq.maps._svcb1.geocoder0 获取经纬度
                val entity = template.getForEntity("https://apis.map.qq.com/jsapi?qt=geoc&addr={0}&key=TU5BZ-MKD3W-L43RW-O3ZBW-GWMZK-QBB25&output=json&pf=jsapi&ref=jsapi&cb=qq.maps._svcb1.geocoder0", Map::class.java, key).body

                val valext = "${entity["pointy"]},${entity["pointx"]}"
                lines.add("$key=${value.trim()},$valext")
            }
        }
        file.writeText(lines.joinToString("\n"))
    }

    @Test
    fun getProvinces() {
        System.err.println(StringUtil.json(GB2260.provinces, true))
    }

    @Test
    fun getDivision() {
        System.err.println(StringUtil.json(GB2260.getDivision("411523"), true))
        System.err.println("=============================================================")
        System.err.println(StringUtil.json(GB2260.getDivision("130728"), true))
    }

    @Test
    fun getDivisionByName() {
        System.err.println(
                StringUtil.json(
                        GB2260.getDivisionByName(
                                listOf(
                                        "河北省",
                                        "张家口市",
                                        "怀安县"
                                )
                        ), true
                )
        )
        System.err.println("=============================================================")
        System.err.println(
                StringUtil.json(
                        GB2260.getDivisionByName(
                                listOf(
                                        "河北省",
                                        "张家口市"
                                )
                        ), true
                )
        )
    }

    @Test
    fun info() {
        var division = GB2260.getDivision("130000")
        System.err.println(division.province)
        System.err.println(division.prefecture)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
        System.err.println("==========================")
        division = GB2260.getDivision("130700")
        System.err.println(division.province)
        System.err.println(division.prefecture)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
        System.err.println("==========================")
        division = GB2260.getDivision("130728")
        System.err.println(division.province)
        System.err.println(division.prefecture)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
        division = GB2260.getDivision("110000")
        System.err.println(division.province)
        System.err.println(division.prefecture)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
        division = GB2260.getDivision("110101")
        System.err.println(division.province)
        System.err.println(division.prefecture)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
    }
}