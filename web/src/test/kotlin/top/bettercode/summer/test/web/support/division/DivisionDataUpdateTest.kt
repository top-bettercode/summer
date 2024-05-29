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
        File("build/division.sqlite").delete()
        connection = DriverManager.getConnection("jdbc:sqlite:build/division.sqlite")
        println("数据库连接成功！")
        val stmt = connection.createStatement()
        //统计用区划代码	城乡分类代码	名称
        val sql =
            """CREATE TABLE IF NOT EXISTS division (
                |CODE CHAR(12) PRIMARY KEY  NOT NULL,
                |PARENT_CODE   CHAR(12)    NULL,
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
        }
        elements.forEach { el1 ->
            val provinceHref: String? = el1.attr("href")
            val provinceCode = provinceHref!!.substringBefore(".html").padEnd(12, '0')
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
            System.err.println("$provinceCode:$provinceName:$provinceHref")
            //city
            val provinceUrl = indexUrl.substringBeforeLast("/") + "/" + provinceHref
            elements = document(provinceUrl).select("tr.citytr")
            if (elements.isEmpty()) {
                file(provinceUrl).delete()
            }
            elements.forEach { el2 ->
                var es = el2.select("td")
                var codetd = es[0]
                val cityCode = codetd.text().padEnd(12, '0')
                val cityHref = codetd.selectFirst("a")?.attr("href")
                val cityName = es[1].text().trim()
                datas.add(
                    DivisionData(
                        code = cityCode,
                        parentCode = provinceCode,
                        name = cityName,
                        fullName = provinceName + cityName,
                        level = 2
                    )
                )
                System.err.println("$cityCode:$cityName:$cityHref")
                //county
                if (!cityHref.isNullOrBlank()) {
                    val cityUrl = provinceUrl.substringBeforeLast("/") + "/" + cityHref
                    elements = document(cityUrl).select("tr.countytr")
                    if (elements.isEmpty()) {
                        file(cityUrl).delete()
                    }
                    elements.forEach { el3 ->
                        es = el3.select("td")
                        codetd = es[0]
                        val countyCode = codetd.text().padEnd(12, '0')
                        val countyHref = codetd.selectFirst("a")?.attr("href")
                        val countyName = es[1].text().trim()
                        datas.add(
                            DivisionData(
                                code = countyCode,
                                parentCode = cityCode,
                                name = countyName,
                                fullName = provinceName + cityName + countyName,
                                level = 3
                            )
                        )
                        System.err.println("$countyCode:$countyName:$countyHref")
                        //town
                        if (!countyHref.isNullOrBlank()) {
                            val countyUrl =
                                cityUrl.substringBeforeLast("/") + "/" + countyHref
                            elements = document(countyUrl).select("tr.towntr")
                            if (elements.isEmpty()) {
                                file(countyUrl).delete()
                            }
                            elements.forEach { el4 ->
                                es = el4.select("td")
                                codetd = es[0]
                                val townCode = codetd.text().padEnd(12, '0')
                                val townHref = codetd.selectFirst("a")?.attr("href")
                                val townName = es[1].text().trim()
                                datas.add(
                                    DivisionData(
                                        code = townCode,
                                        parentCode = countyCode,
                                        name = townName,
                                        fullName = provinceName + cityName + countyName + townName,
                                        level = 4
                                    )
                                )

                                System.err.println("$townCode:$townName:$townHref")
                                //village
                                if (!townHref.isNullOrBlank()) {
                                    val townUrl =
                                        countyUrl.substringBeforeLast("/") + "/" + townHref
                                    elements = document(townUrl).select("tr.villagetr")
                                    if (elements.isEmpty()) {
                                        file(townUrl).delete()
                                    }
                                    elements.forEach { el5 ->
                                        es = el5.select("td")
                                        codetd = es[0]
                                        val villageCode = codetd.text().padEnd(12, '0')
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

                                        System.err.println(
                                            "$villageCode:${
                                                villageName + " #" + es[1].text().trim()
                                            }"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        """710000=台湾省
810000=香港特别行政区
820000=澳门特别行政区
""".trimIndent().split("\n").forEach {
            it.split("=").let { ss ->
                datas.add(
                    DivisionData(
                        code = ss[0],
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
        }
        statement.close()
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
        System.err.println("jsoupDoc:$url")
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (X11; Linux x86_64; rv:126.0) Gecko/20100101 Firefox/126.0")
            .get()
        val text = doc.html()
        return text
    }

    private fun downWebPage(url: String): String {
        System.err.println("downWebPage:$url")
        // 访问目标网页
        driver[url]
        // 获取页面源代码
        return driver.pageSource
    }

    data class DivisionData(
        val code: String,
        val parentCode: String?,
        val name: String,
        val fullName: String,
        val level: Int,
        val typeCode: String? = null
    )
}