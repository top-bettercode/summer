package top.bettercode.summer.tools.excel

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.capitalized
import java.io.File
import java.util.*

/**
 * @author Peter Wu
 */
class TempTest {


    //@Disable
    @Test
    fun gen() {
        val dir = File("/data/repositories/bettercode/wintruelife/discard/delivery")

        dir.walkTopDown().filter { it.isFile && it.name.endsWith(".java") && it.readText().contains("@ExcelField") }.forEach { file ->
            val className = file.nameWithoutExtension
            val codes = mutableMapOf<Int, String>()
            val readLines = file.readLines()
            for (i in readLines.indices) {
                val line = readLines[i].trim()
                //如果是以@ExcelField开头
                if (line.startsWith("@ExcelField")) {
                    //提取@ExcelField(title = "商品分类名称", sort = 1)中的"商品分类名称"字符
                    val title = line.substringAfter("title = \"").substringBefore("\"")
                    //sort = 1
                    val sort = line.substringAfter("sort = ", "0").substringBefore(")").substringBefore(",").trim().toInt()
                    // 是否  YuanConverter
                    val isYuanConverter = line.contains("YuanConverter")
                    // 是否  MoneyToConverter
                    val isMoneyToConverter = line.contains("MoneyToConverter")
                    // 是否 CodeConverter
                    val isCodeConverter = line.contains("CodeConverter")
                    // 是否  WeightToConverter
                    val isWeightToConverter = line.contains("WeightToConverter")
                    //DateConverter.class, pattern = "yyyy-MM-dd HH:mm:ss"
                    val isDateConverter = line.contains("DateConverter")
                    val format = line.substringAfter("pattern = \"").substringBefore("\"").trim().lowercase(Locale.getDefault())
                    // 是否  Converter
                    val isConverter = line.contains("converter")
                    // converter = WeightToConverter.class
                    val converter = line.substringAfter("converter = ").substringBefore(")")

                    val readName = findReadName(readLines, i)
                    //ExcelField.of("商品分类名称", OrderReceivablesCusto::getCommoTyName),
                    codes[sort] = ("""
                    ExcelField.of("$title", $className::${readName})${
                        if (isYuanConverter||isMoneyToConverter) {
                            ".yuan()"
                        } else if (isCodeConverter) {
                            ".code()"
                        } else if (isWeightToConverter) {
                            ".unit(1000, 3)"
                        } else if (isDateConverter) {
                            ".format($format)"
                        } else if (isConverter) {
                            ".converter(${converter})"
                        } else {
                            ""
                        }
                    },
                    """.trimIndent())

                }
            }

            if (codes.isNotEmpty()) {
                System.err.println("======================================")
                var code = "private final ExcelField<$className, ?>[] excelFields = ArrayUtil.of(\n"
                codes.keys.sorted().forEach {
                    code += codes[it] + "\n"
                }
                code += ");"
                println(code)
            }
        }
        System.err.println("======================================")
    }

    fun findReadName(lines: List<String>, i: Int): String {
        return if (i + 1 < lines.size) {
            val readName = lines[i + 1].trim()
            //如果符合private String commoTyName;返回，如果不符合继续查找
            if (readName.startsWith("private ")) {
                "get${readName.replace("private \\S* (.*?);.*".toRegex(), "$1").trim().capitalized()}"
            } else if (readName.startsWith("public ")) {
                //public String getBrandName()
                readName.replace("public \\S*? (.*?)\\(.*".toRegex(), "$1").trim()
            } else {
                findReadName(lines, i + 1)
            }
        } else {
            ""
        }
    }

    @Test
    fun name1() {
        System.err.println(Int::class.java.name)
        System.err.println("0." + String.format("%0" + 2 + "d", 0))
    }

    @Test
    fun name() {
        for (c in 0..199) {
            val x = getString(c)
            System.err.println(x)
        }
    }

    private fun getString(i: Int): String {
        var i1 = i
        val chars = StringBuilder()
        do {
            chars.append(('A'.code + i1 % 26).toChar())
        } while ((i1 / 26 - 1).also { i1 = it } >= 0)
        return chars.reverse().toString()
    }
}
