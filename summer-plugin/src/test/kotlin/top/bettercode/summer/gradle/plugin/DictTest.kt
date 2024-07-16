package top.bettercode.summer.gradle.plugin

import com.hankcs.hanlp.HanLP
import com.hankcs.hanlp.dictionary.CustomDictionary
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.generator.SortedProperties
import java.io.File

/**
 *
 * @author Peter Wu
 */
class DictTest {

    @Test
    fun test() {
        val file = File("src/test/resources/cedict_ts.u8")
        val properties = SortedProperties()
        file.readLines().forEach { line ->
            if (line.startsWith("#")) {
                return@forEach
            }
            val firstPart = line.substringBefore("/")
            val key = firstPart.split(" ")[1].trim()
            if ("是" == key) {
                System.err.println(key)
            }
            var value = line.substringAfter("/").trim().trim('/')
            value = value.split("/").map { s ->
                val trim = s.replace(Regex("\\[[^]]+?]"), "")
                    .replace(Regex("\\([^)]+?\\)"), "").trim()
                val strs = trim.split(";", "；")
                strs.map { it.trim().substringAfter("to ").substringAfter("a ") }
                    .minByOrNull { it.length } ?: ""
            }.filter { it.isNotEmpty() && it.matches(Regex("[a-zA-Z0-9' -]+")) }
                .map { it.trim() }.minByOrNull { it.length }?.trim() ?: ""

            if (key.isNotBlank() && value.isNotBlank() && properties[key] == null) {
                properties[key] = value
            }
        }
        properties.store(
            File("src/main/resources/default-dict.properties").outputStream(),
            "汉英字典"
        )
    }

    @Test
    fun segment() {
        val text = "拼车管理"
        CustomDictionary.add("拼车管理")
        CustomDictionary.add("拼车")
        val segmentList = HanLP.segment(text)
        println(segmentList)
    }
}