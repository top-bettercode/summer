package top.bettercode.summer.test.web.support.division

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
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
@Disabled
class DivisionDataUpdateTest {

    val driver: WebDriver
    var fetchLevel: Int = 5

    init {
        // 设置 ChromeDriver 的路径
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver")
        // 配置 ChromeOptions
        val options = ChromeOptions()
        options.addArguments("--headless") // 无头模式
        // 创建 WebDriver 实例
        driver = ChromeDriver(options)
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
        System.err.println("开始获取数据")
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
            val hasProvince = !provinceHref.isNullOrBlank()
            val provinceCode = provinceHref!!.substringBefore(".html").padEnd(12, '0').toLong()
            val provinceName = el1.text().trim()
            datas.add(
                DivisionData(
                    code = provinceCode,
                    name = provinceName,
                    level = 1,
                    leaf = if (hasProvince) 0 else 1,
                    parentCode = null,
                    codePath = provinceCode.toString(),
                    namePath = provinceName,
                )
            )
            System.err.print(".")
            System.err.flush()
            if (hasProvince) {
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
                    val hasCounty = !cityHref.isNullOrBlank()
                    datas.add(
                        DivisionData(
                            code = cityCode,
                            name = cityName,
                            level = 2,
                            leaf = if (hasCounty) 0 else 1,
                            parentCode = provinceCode,
                            codePath = "$provinceCode>$cityCode",
                            namePath = "$provinceName>$cityName"
                        )
                    )
                    System.err.print(".")
                    System.err.flush()
                    //county
                    if (hasCounty) {
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
                            val hasTown = fetchLevel >= 4 && !countyHref.isNullOrBlank()
                            if (countyLevel != 4 || fetchLevel >= 4)
                                datas.add(
                                    DivisionData(
                                        code = countyCode,
                                        name = countyName,
                                        level = countyLevel,
                                        leaf = if (hasTown) 0 else 1,
                                        parentCode = cityCode,
                                        codePath = "$provinceCode>$cityCode>$countyCode",
                                        namePath = "$provinceName>$cityName>$countyName"
                                    )
                                )
                            System.err.print(".")
                            System.err.flush()
                            //town
                            if (hasTown) {
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
                                    val hasVillage = fetchLevel >= 5 && !townHref.isNullOrBlank()
                                    if (townLevel != 5 || fetchLevel >= 5)
                                        datas.add(
                                            DivisionData(
                                                code = townCode,
                                                name = townName,
                                                level = townLevel,
                                                leaf = if (hasVillage) 0 else 1,
                                                parentCode = countyCode,
                                                codePath = "$provinceCode>$cityCode>$countyCode>$townCode",
                                                namePath = "$provinceName>$cityName>$countyName>$townName"
                                            )
                                        )
                                    System.err.print(".")
                                    System.err.flush()

                                    //village
                                    if (hasVillage) {
                                        val townUrl =
                                            countyUrl.substringBeforeLast("/") + "/" + townHref
                                        elements = document(townUrl).select("tr.villagetr")
                                        if (elements.isEmpty()) {
                                            file(townUrl).delete()
                                            throw RuntimeException(
                                                "fail ${
                                                    townUrl.substringAfterLast(
                                                        "/"
                                                    )
                                                }"
                                            )
                                        }
                                        elements.forEach { el5 ->
                                            es = el5.select("td")
                                            codetd = es[0]
                                            val villageCode = codetd.text().padEnd(12, '0').toLong()
                                            val villageName = es[2].text().trim()
                                            val typeCode = es[1].text().trim().toInt()

                                            datas.add(
                                                DivisionData(
                                                    code = villageCode,
                                                    name = villageName,
                                                    level = 5,
                                                    leaf = 1,
                                                    parentCode = townCode,
                                                    codePath = "$provinceCode>$cityCode>$countyCode>$townCode>$villageCode",
                                                    namePath = "$provinceName>$cityName>$countyName>$townName>$villageName",
                                                    typeCode = typeCode
                                                )
                                            )
                                            System.err.print(".")
                                            System.err.flush()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.err.println()
        System.err.println("====数据获取完成====")

        """710000=台湾省
810000=香港特别行政区
820000=澳门特别行政区
""".trimIndent().split("\n").forEach {
            it.split("=").let { ss ->
                val name = ss[1]
                val code = ss[0].padEnd(12, '0').toLong()
                datas.add(
                    DivisionData(
                        code = code,
                        name = name,
                        level = 1,
                        leaf = 0,
                        parentCode = null,
                        codePath = code.toString(),
                        namePath = name
                    )
                )
            }
        }

        Class.forName("org.sqlite.JDBC")
        val dbFile = File("division.sqlite")
        dbFile.delete()
        val url = "jdbc:sqlite:$dbFile"
        DriverManager.getConnection(url).use { conn ->
            conn.autoCommit = false // 关闭自动提交
            conn.createStatement().use {
                it.executeUpdate(
                    """CREATE TABLE IF NOT EXISTS DIVISION (
                |CODE INT PRIMARY KEY      NOT NULL,
                |NAME          CHAR(50)    NOT NULL,
                |LEVEL         INT         NOT NULL,
                |LEAF          INT         NOT NULL,
                |PARENT_CODE   INT         NULL,
                |CODE_PATH     CHAR(100)   NOT NULL,
                |NAME_PATH     CHAR(200)   NOT NULL,
                |TYPE_CODE     INT     NULL
                |)""".trimMargin()
                )
            }
            conn.commit()

            val sql =
                "INSERT INTO DIVISION (CODE,NAME,LEVEL,LEAF,PARENT_CODE,CODE_PATH,NAME_PATH,TYPE_CODE) VALUES (?,?,?,?,?,?,?,?)"
            conn.prepareStatement(sql).use { pstmt ->
                datas.forEachIndexed { i, it ->
                    pstmt.setLong(1, it.code)
                    pstmt.setString(2, it.name)
                    pstmt.setInt(3, it.level)
                    pstmt.setInt(4, it.leaf)
                    pstmt.setObject(5, it.parentCode)
                    pstmt.setString(6, it.codePath)
                    pstmt.setString(7, it.namePath)
                    pstmt.setObject(8, it.typeCode)
                    pstmt.addBatch() // 添加到批处理
                    System.err.print("-")
                    System.err.flush()

                    if (i % 100 == 0) { // 每100条提交一次
                        pstmt.executeBatch()
                        conn.commit()
                    }
                }
                pstmt.executeBatch() // 提交剩余的记录
                conn.commit()
            }
        }
        System.err.println()
        System.err.println("====完成====")
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
        val name: String,
        val level: Int,
        val leaf: Int,
        val parentCode: Long?,
        val codePath: String,
        val namePath: String,
        val typeCode: Int? = null
    )
}