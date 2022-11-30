package top.bettercode.autodoc.core

import com.github.stuxuhai.jpinyin.PinyinFormat
import com.github.stuxuhai.jpinyin.PinyinHelper
import org.junit.jupiter.api.Test
import top.bettercode.autodoc.core.model.Field
import java.io.File
import java.util.*

/**
 *
 * @author Peter Wu
 */
class Temp {

    @Test
    fun pinyin() {
        println(PinyinHelper.convertToPinyinString("手势密码新增-、修改", "", PinyinFormat.WITHOUT_TONE).replace("[^\\x00-\\xff]".toRegex(),"").replace("\\s*|\t|\r|\n".toRegex(),""))
    }

    @Test
    fun name() {
        File(Temp::class.java.getResource("/static/Open+Sans.css")!!.file).readLines().filter { it.contains("url(https:") }.forEach {
            println(it.replace(".*url\\((.*?)\\).*".toRegex(), "$1"))
        }
    }

    @Test
    fun cp() {
        File(AutodocExtension::class.java.getResource("/static")!!.file).walkTopDown().filter { it.isFile }.forEach {
            val path = it.path.replace("/data/repositories/bettercode/default/autodoc/core/build/resources/main/", "")
            System.err.println("AutodocExtension::class.java.getResourceAsStream(\"/$path\").copyTo(File(outputFile, \"$path\").outputStream())")
        }
    }

    @Test
    fun fieldSet() {
        val fields: TreeSet<Field> = TreeSet()
        val f1 = Field("a", description = "")
        val f2 = Field("a", description = "b")
        val f3 = Field("b.a", description = "b")
        val f4 = Field("c.a", description = "")
        fields.add(f1)
        System.err.println(fields.contains(f2))
        fields.add(f2)
        fields.add(f3)
        fields.add(f4)
        System.err.println(fields)
    }

    @Test
    fun fieldSet2() {
        val fields: TreeSet<Field> = TreeSet()
        val f1 = Field("a", description = "b")
        val f2 = Field("b", description = "b")
        fields.add(f1)
        System.err.println(fields.contains(f2))
        fields.add(f2)
        System.err.println(fields)
    }
}