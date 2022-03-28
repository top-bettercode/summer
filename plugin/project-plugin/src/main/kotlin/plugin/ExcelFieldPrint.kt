package plugin

import top.bettercode.generator.dsl.Generator
import java.util.*

/**
 * @author Peter Wu
 */
open class ExcelFieldPrint : Generator() {

    override fun call() {
        println("""private final ExcelField<$className, ?>[] excelFields = ArrayUtil.of(""")
        val cols = columns
        val size = cols.size
        cols.forEachIndexed { i, it ->
            val code =
                if (it.isCodeField) {
                    if (it.columnName.contains("_") || it.isSoftDelete) ".code()" else ".code(${
                        (className + it.javaName.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.getDefault()
                            ) else it.toString()
                        })
                    }Enum.ENUM_NAME)"
                } else {
                    ""
                }
            val propertyGetter =
                if (it.isPrimary && primaryKeys.size > 1) "${it.javaType.shortNameWithoutTypeArguments}.class, from -> from.get${
                    primaryKeyName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                }().get${it.javaName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}()" else "$className::get${
                    it.javaName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                }"
            println("      ExcelField.of(\"${it.remark.split(Regex("[:：,， (（]"))[0]}\", $propertyGetter)${code}${if (i == size - 1) "" else ","}")
        }
        println("""  );""")
    }
}