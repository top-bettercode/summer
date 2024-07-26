package top.bettercode.summer.gradle.plugin.project.update

import org.gradle.api.Project
import top.bettercode.summer.gradle.plugin.project.DicCodeGen

/**
 *
 * @author Peter Wu
 */
class ExcelUpdate {

    fun update(project: Project) {
        val replaceCodeNames: MutableMap<String, String> = mutableMapOf()
        project.logger.lifecycle("更新代码")

        replaceCodeNames["top.bettercode.summer.tools.excel.ExcelImport"] =
            "top.bettercode.summer.tools.excel.read.ExcelReader"
        replaceCodeNames["top.bettercode.summer.tools.excel.ExcelExport"] =
            "top.bettercode.summer.tools.excel.write.ExcelWriter"
        replaceCodeNames["|||import top.bettercode.summer.tools.excel.ExcelField;"] =
            "import top.bettercode.summer.tools.excel.write.CellSetter;\n" +
                    "import top.bettercode.summer.tools.excel.write.RowSetter;"
        replaceCodeNames["ExcelImport"] = "ExcelReader"
        replaceCodeNames["excelImport.setRow"] = "reader.row"
        replaceCodeNames["excelImport"] = "reader"
        replaceCodeNames["ExcelExport.export"] = "ExcelWriter.write"
        replaceCodeNames["ExcelExport"] = "ExcelWriter"
        replaceCodeNames["***ExcelField<(.*?), \\?>\\[\\] (.*?) = ArrayUtil\\.of\\("] =
            "RowSetter<\$1> \$2 = RowSetter.of("
        replaceCodeNames["ExcelField"] = "CellSetter"
        replaceCodeNames["excelFields"] = "rowSetter"
        replaceCodeNames["IExcel"] = "Excel"
        replaceCodeNames["|||).cell("] = ").converter("

        DicCodeGen.replaceOld(project, replaceCodeNames)
        project.logger.lifecycle("更新代码完成")
    }
}