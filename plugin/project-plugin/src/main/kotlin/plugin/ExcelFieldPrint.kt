package plugin

import top.bettercode.generator.dsl.Generator
import top.bettercode.lang.capitalized

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
                    if (it.columnName.contains("_") || it.softDelete) ".code()" else ".code(${
                        (className + it.javaName.capitalized())
                    }Enum.ENUM_NAME)"
                } else {
                    ""
                }
            val propertyGetter =
                if (it.isPrimary && primaryKeys.size > 1) "${it.javaType.shortNameWithoutTypeArguments}.class, from -> from.get${
                    primaryKeyName.capitalized()
                }().get${it.javaName.capitalized()}()" else "$className::get${
                    it.javaName.capitalized()
                }"
            println("      ExcelField.of(\"${it.remark.split(Regex("[:：,， (（]"))[0]}\", $propertyGetter)${code}${if (i == size - 1) "" else ","}")
        }
        println("""  );""")
    }
}