package top.bettercode.summer.test.web.support.division

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File
import java.sql.DriverManager


/**
 *
 * @author Peter Wu
 */
class DivisionDataUpdateTest {

    val connection: java.sql.Connection
    val driver: WebDriver

    init {
        // 设置 ChromeDriver 的路径
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver")
        // 配置 ChromeOptions
        val options = ChromeOptions()
        options.addArguments("--headless") // 无头模式
        // 创建 WebDriver 实例
        driver = ChromeDriver(options)

        Class.forName("org.sqlite.JDBC")
        File("division.sqlite").delete()
        connection = DriverManager.getConnection("jdbc:sqlite:division.sqlite")
        println("数据库连接成功！")
        val stmt = connection.createStatement()
        //统计用区划代码	城乡分类代码	名称
        val sql =
            """CREATE TABLE IF NOT EXISTS division (
                |CODE INT PRIMARY KEY  NOT NULL,
                |PARENT_CODE   INT    NULL,
                |NAME          CHAR(200)   NOT NULL,
                |FULL_NAME     CHAR(500)   NOT NULL,
                |LEVEL         INT         NOT NULL,
                |TYPE_CODE     CHAR(3)     NULL
                |)""".trimMargin()
        stmt.executeUpdate(sql)
        stmt.close()
    }

    @AfterEach
    fun tearDown() {
        driver.quit()
    }

    /**
     *
     * 市辖区
     * 省直辖县级行政区划
     * 自治区直辖县级行政区划
     * 县
     * 县直辖村级区划
     * 区直辖村级区划
     * https://www.stats.gov.cn/sj/tjbz/qhdm/ 2023年6月30日
     */
    @Test
    fun get() {
        val indexUrl = "https://www.stats.gov.cn/sj/tjbz/tjyqhdmhcxhfdm/2023/index.html"
        //province
        val datas = mutableListOf<DivisionData>()
        var elements = document(indexUrl).select(".provincetr td a")
        if (elements.isEmpty()) {
            file(indexUrl).delete()
            throw RuntimeException("fail ${indexUrl.substringAfterLast("/")}")
        }
        elements.forEach { el1 ->
            val provinceHref: String? = el1.attr("href")
            val provinceCode = provinceHref!!.substringBefore(".html").padEnd(12, '0').toLong()
            val provinceName = el1.text().trim()
            datas.add(
                DivisionData(
                    code = provinceCode,
                    parentCode = null,
                    name = provinceName,
                    fullName = provinceName,
                    level = 1
                )
            )
            System.err.print(".")
            //city
            val provinceUrl = indexUrl.substringBeforeLast("/") + "/" + provinceHref
            elements = document(provinceUrl).select("tr.citytr")
            if (elements.isEmpty()) {
                file(provinceUrl).delete()
                throw RuntimeException("fail ${provinceUrl.substringAfterLast("/")}")
            }
            elements.forEach { el2 ->
                var es = el2.select("td")
                var codetd = es[0]
                val cityCode = codetd.text().padEnd(12, '0').toLong()
                val cityHref = codetd.selectFirst("a")?.attr("href")
                val cityName = es[1].text().trim()
//                if (cityName == "县" && provinceCode == 500000000000L) {
//                    cityName = "重庆县"
//                }
                datas.add(
                    DivisionData(
                        code = cityCode,
                        parentCode = provinceCode,
                        name = cityName,
                        fullName = provinceName + cityName,
                        level = 2
                    )
                )
                System.err.print(".")
                //county
                if (!cityHref.isNullOrBlank()) {
                    val cityUrl = provinceUrl.substringBeforeLast("/") + "/" + cityHref
                    val cityDoc = document(cityUrl)
                    elements = cityDoc.select("tr.countytr")
                    var countyLevel = 3
                    //兼容
                    if (elements.isEmpty()) {
                        countyLevel = 4
                        elements = cityDoc.select("tr.towntr")
                    }
                    if (elements.isEmpty()) {
                        file(cityUrl).delete()
                        throw RuntimeException("fail ${cityUrl.substringAfterLast("/")}")
                    }
                    elements.forEach { el3 ->
                        es = el3.select("td")
                        codetd = es[0]
                        val countyCode = codetd.text().padEnd(12, '0').toLong()
                        val countyHref = codetd.selectFirst("a")?.attr("href")
                        val countyName = es[1].text().trim()
                        datas.add(
                            DivisionData(
                                code = countyCode,
                                parentCode = cityCode,
                                name = countyName,
                                fullName = provinceName + cityName + countyName,
                                level = countyLevel
                            )
                        )
                        System.err.print(".")
                        //town
                        if (!countyHref.isNullOrBlank()) {
                            val countyUrl =
                                cityUrl.substringBeforeLast("/") + "/" + countyHref
                            elements = document(countyUrl).select("tr.towntr")
                            var townLevel = 4
                            if (elements.isEmpty()) {
                                townLevel = 5
                                elements = document(countyUrl).select("tr.villagetr")
                            }
                            if (elements.isEmpty()) {
                                file(countyUrl).delete()
                                throw RuntimeException("fail ${countyUrl.substringAfterLast("/")}")
                            }
                            elements.forEach { el4 ->
                                es = el4.select("td")
                                codetd = es[0]
                                val townCode = codetd.text().padEnd(12, '0').toLong()
                                val townHref = codetd.selectFirst("a")?.attr("href")
                                val townName = es[1].text().trim()
                                datas.add(
                                    DivisionData(
                                        code = townCode,
                                        parentCode = countyCode,
                                        name = townName,
                                        fullName = provinceName + cityName + countyName + townName,
                                        level = townLevel
                                    )
                                )
                                System.err.print(".")

                                //village
                                if (!townHref.isNullOrBlank()) {
                                    val townUrl =
                                        countyUrl.substringBeforeLast("/") + "/" + townHref
                                    elements = document(townUrl).select("tr.villagetr")
                                    if (elements.isEmpty()) {
                                        file(townUrl).delete()
                                        throw RuntimeException("fail ${townUrl.substringAfterLast("/")}")
                                    }
                                    elements.forEach { el5 ->
                                        es = el5.select("td")
                                        codetd = es[0]
                                        val villageCode = codetd.text().padEnd(12, '0').toLong()
                                        val villageName = es[2].text().trim()
                                        val typeCode = es[1].text().trim()

                                        datas.add(
                                            DivisionData(
                                                code = villageCode,
                                                parentCode = townCode,
                                                name = villageName,
                                                fullName = provinceName + cityName + countyName + townName + villageName,
                                                level = 5,
                                                typeCode = typeCode
                                            )
                                        )
                                        System.err.print(".")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.err.println("====数据获取完成====")
        """710000=台湾省
810000=香港特别行政区
820000=澳门特别行政区
""".trimIndent().split("\n").forEach {
            it.split("=").let { ss ->
                datas.add(
                    DivisionData(
                        code = ss[0].padEnd(12, '0').toLong(),
                        parentCode = null,
                        name = ss[1],
                        fullName = ss[1],
                        level = 1
                    )
                )
            }
        }
        val statement = connection.createStatement()
        datas.forEach {
            val sql =
                "INSERT INTO division (CODE, PARENT_CODE, NAME, FULL_NAME, LEVEL, TYPE_CODE) VALUES ('${it.code}', ${if (it.parentCode == null) null else "'${it.parentCode}'"}, '${it.name}', '${it.fullName}', ${it.level}, ${if (it.typeCode == null) null else "'${it.typeCode}'"})"
            statement.executeUpdate(sql)
            System.err.print("-")
        }
        statement.close()
        System.err.println("====完成====")
    }

    @Test
    fun getDoc() {
        document("https://www.stats.gov.cn/sj/tjbz/tjyqhdmhcxhfdm/2023/33/11/23/331123204.html")
    }

    private fun document(url: String): Document {
        val file = file(url)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        if (!file.exists()) {
//            val text = jsoupDoc(url)
            val text = downWebPage(url)
            file.writeText(text)
        }
        return Jsoup.parse(file)
    }

    private fun file(url: String) =
        File(System.getProperty("user.home") + "/.cache/division/" + url.substringAfterLast("/"))

    private fun jsoupDoc(url: String): String {
        System.err.println()
        System.err.println("download:$url")
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (X11; Linux x86_64; rv:126.0) Gecko/20100101 Firefox/126.0")
            .get()
        val text = doc.html()
        return text
    }

    private fun downWebPage(url: String): String {
        System.err.println()
        System.err.println("download:$url")
        // 访问目标网页
        driver[url]
        // 获取页面源代码
        return driver.pageSource
    }

    data class DivisionData(
        val code: Long,
        val parentCode: Long?,
        val name: String,
        val fullName: String,
        val level: Int,
        val typeCode: String? = null
    )
}