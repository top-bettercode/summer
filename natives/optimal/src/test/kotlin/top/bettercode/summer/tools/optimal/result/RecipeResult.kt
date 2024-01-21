package top.bettercode.summer.tools.optimal.result

import org.dhatim.fastexcel.BorderStyle
import org.dhatim.fastexcel.Color
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import org.springframework.util.StringUtils
import top.bettercode.summer.tools.excel.Alignment
import top.bettercode.summer.tools.excel.ColumnWidths
import top.bettercode.summer.tools.optimal.entity.*
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale
import java.io.File
import java.nio.file.Files

/**
 * 配方计算结果
 *
 * @author Peter Wu
 */
class RecipeResult // --------------------------------------------
(
        private val solverName: String,
        /** 配方要求  */
        private val reqData: ReqData) {
    // --------------------------------------------

    /** 配方  */
    var recipes: MutableList<Recipe> = ArrayList()

    /** 耗时  */
    var time: Long = 0

    /** 计算次数  */
    var solveCount = 0

    // --------------------------------------------
    fun addRecipe(recipe: Recipe) {
        recipes.add(recipe)
    }

    /** 增加次数  */
    fun addSolveCount() {
        solveCount++
    }

    val recipeCount: Int
        /**
         * @return 配方数量
         */
        get() = recipes.size


    // --------------------------------------------
    // 输出 Excel
    fun toExcel() {
        // 结果输出
        val size = recipes.size
        if (size > 0) {
            val outFile = File(
                    "build/"
                            + solverName
                            + "-${reqData.fileName}"
                            + "-推"
                            + size
                            + "个-"
                            + System.currentTimeMillis()
                            + ".xlsx")
            outFile.getParentFile().mkdirs()
            val workbook = Workbook(Files.newOutputStream(outFile.toPath()), "", "1.0")
            var sheet = workbook.newWorksheet("最终候选原料")
            val titles = ("原料名称 价格 " + Components.COMPONENT_NAME_STRING).split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var r = 0
            var c = 0
            for (i in titles.indices) {
                value(sheet, r, c + i, titles[i])
            }
            val reqMaterials: Collection<Material> = reqData.materials.values
            for (matrial in reqMaterials) {
                val matrialName = matrial.name
                var cc = 0
                // 原料名称
                value(sheet, ++r, cc++, matrialName)
                // 成本
                // 单价
                value(sheet, r, cc++, matrial.price)
                val components = matrial.components
                // 原料成份
                for (index in components!!.keys) {
                    value(
                            sheet,
                            r,
                            cc + index, (components[index]
                    !!.value!! * 100.0).scale(2)
                            .toString() + "%")
                }
            }
            //
            var firstSheet: Worksheet? = null
            for (i in recipes.indices) {
                sheet = workbook.newWorksheet("配方" + (i + 1))
                if (firstSheet == null) {
                    firstSheet = sheet
                }
                val recipe = recipes[i]
                sheet.width(0, 25.0)
                sheet.width(1, ColumnWidths.getWidth("最小用量") + 2)
                sheet.width(2, ColumnWidths.getWidth("最小用量") + 2)
                r = 0
                value(sheet, r, 0, "配方成本：")
                value(sheet, r, 1, recipe.cost?.scale(2))
                r++
                value(sheet, r, 0, "产品量：")
                val targetWeight = reqData.targetWeight
                value(sheet, r, 1, targetWeight)
                val dryWater = recipe.dryWater
                if (reqData.isAllowDrying) {
                    value(sheet, r, 2, "至少需要哄干的水分：")
                    sheet.range(r, 2, r, 3).merge()
                    value(sheet, r, 4, dryWater!!.scale(2))
                }
                r++
                val notMixMaterials = reqData.notMixMaterials
                if (notMixMaterials!!.isNotEmpty()) {
                    // 不能混用原料限制
                    value(sheet, r, 0, "不能混用限制条件：")
                    val str = notMixMaterials.joinToString("，") { arr: Array<String>? -> StringUtils.arrayToDelimitedString(arr, "和") }
                    value(sheet, r, 1, str)
                    sheet.style(r, 1).horizontalAlignment(Alignment.LEFT.value).set()
                    sheet.range(r, 1, r, 14).merge()
                }
                r++
                value(sheet, r, 0, "不使用的原料：")
                val notUseMaterialNames = reqData.notUseMaterialNames
                value(sheet, r, 1, StringUtils.collectionToDelimitedString(notUseMaterialNames, "，"))
                sheet.style(r, 1).horizontalAlignment(Alignment.LEFT.value).set()
                sheet.range(r, 1, r, 14).merge()
                r++
                value(sheet, r, 0, "限制条件：")
                val limitMaterialStrings: MutableList<String?> = ArrayList()
                val componentTarget = reqData.componentTarget
                val componentNames: List<String> = Components.componentNames
                for (integer in componentTarget.keys) {
                    val limit = componentTarget[integer]
                    if (limit?.materials != null) {
                        val componentName = componentNames[integer]
                        limitMaterialStrings.add(
                                componentName
                                        + "只能用："
                                        + StringUtils.collectionToDelimitedString(limit.materials, "，"))
                    }
                }
                reqData
                        .materialReq
                        ?.forEach { (name: String?, limit: Limit?) ->
                            if (limit.materials != null) {
                                limitMaterialStrings.add(
                                        name
                                                + "只能用："
                                                + StringUtils.collectionToDelimitedString(limit.materials, "，"))
                            }
                        }
                reqData
                        .conditions
                        ?.forEach { (condition1: Condition?, condition2: Condition?) ->
                            limitMaterialStrings.add(
                                    "当" + condition1.desc + "时，" + condition2.desc)
                        }
                value(sheet, r, 1, StringUtils.collectionToDelimitedString(limitMaterialStrings, "；"))
                sheet.style(r, 1).horizontalAlignment(Alignment.LEFT.value).set()
                sheet.range(r, 1, r, 14).merge()
                r++
                r++
                c = 0
                value(sheet, r, c++, "")
                value(sheet, r, c++, "最小用量")
                value(sheet, r, c++, "最大用量")
                var liquidAmmonia: String = ReqData.LIQUID_AMMONIA
                var la2CAUseRatio = 1.0
                if (recipe.isHascliquidAmmonia) {
                    la2CAUseRatio = ReqData.LA_2_CAUSE_RATIO
                    liquidAmmonia = ReqData.CLIQUID_AMMONIA
                }
                value(sheet, r, c++, "最小耗$liquidAmmonia/硫酸系数")
                value(sheet, r, c++, "最小耗$liquidAmmonia/硫酸量")
                value(sheet, r, c++, "最大耗$liquidAmmonia/硫酸系数")
                value(sheet, r, c++, "最大耗$liquidAmmonia/硫酸量")
                value(sheet, r, c++, "投料量")
                value(sheet, r, c++, "成本")
                value(sheet, r, c++, "单价(/吨)")
                for (j in componentNames.indices) {
                    value(sheet, r, c + j, titles[j + 2])
                }
                r++
                value(sheet, r++, 0, "成份量")
                value(sheet, r++, 0, "目标成份量(最大值)")
                value(sheet, r++, 0, "目标成份量(最小值)")
                value(sheet, r, 0, "成份量(百分比)")
                val row = r - 3
                // 配方目标成份量
                val componentRecipe = recipe.componentRecipe
                for (index in componentTarget.keys) {
                    r = row
                    val c1 = c + index
                    val limit = componentTarget[index]
                    var weight = componentRecipe!![index]?.max
                    val water: Boolean = Components.isWater(index)
                    if (water) {
                        weight = weight!! - dryWater!!
                    }
                    value(sheet, r++, c1, weight!!.scale(2))
                    value(
                            sheet,
                            r++,
                            c1, (limit?.max!! * 100.0).scale(2)
                            .toString() + "%")
                    value(
                            sheet,
                            r++,
                            c1, (limit.min!! * (100.0)).scale(2)
                            .toString() + "%")
                    var v = (weight / targetWeight).scale(4)
                    if (Components.isWaterSolublePhosphorusRate(index))
                        v = (weight / componentRecipe.phosphorus!!.max!!).scale(4)
                    val valid = v >= limit.min!! && v <= limit.max!!
                    value(
                            sheet,
                            r++,
                            c1,
                            ((v * 100.0).scale(2)).toString() + "%",
                            valid)
                }
                var m = 0
                val materials = recipe.materials
                val vitriolMaterialRatioMap = reqData.materialRelations!![ReqData.VITRIOL]
                val liquidAmmoniaMaterialRatioMap = reqData.materialRelations!![ReqData.LIQUID_AMMONIA]
                val limitLiquidAmmonia = reqData.isLimitLiquidAmmonia
                for (material in materials) {
                    val matrialName = material.name
                    val solutionValue = material.solutionValue
                    var cc = 0
                    var mergeRow = 0
                    if (ReqData.VITRIOL == matrialName && limitLiquidAmmonia) {
                        mergeRow = 1
                    } else {
                        if (reqData.isLimitVitriol) for (materialNameFragment in vitriolMaterialRatioMap!!.keys) {
                            if (matrialName!!.contains(materialNameFragment)) {
                                mergeRow = 1
                                break
                            }
                        }
                    }

                    // 原料名称
                    value(sheet, r + m, cc++, matrialName, mergeRow)
                    val materialReq = reqData.materialReq
                    val reqName = materialReq!!.keys.find { s: String -> matrialName!!.contains(s) && materialReq[s]?.min != null }
                    if (reqName != null) {
                        val limit = materialReq[reqName]!!
                        // 最小用量
                        value(sheet, r + m, cc++, limit.min, mergeRow)
                        // 最大用量
                        value(sheet, r + m, cc++, limit.max, mergeRow)
                    } else {
                        cc += 2
                    }
                    if (liquidAmmonia == matrialName) {
                        if (reqData.isLimitLiquidAmmonia) {
                            // 耗液氨系数
                            value(sheet, r + m, cc++, "", mergeRow)
                            // 耗液氨数量
                            sheet.comment(r + m, cc, "最小耗" + liquidAmmonia + "总数量")
                            value(
                                    sheet,
                                    r + m,
                                    cc++,
                                    (recipe
                                            .minLiquidAmmoniaWeight!!
                                            * (la2CAUseRatio))
                                            .scale(2),
                                    mergeRow)
                            // 耗液氨系数
                            value(sheet, r + m, cc++, "", mergeRow)
                            // 耗液氨数量
                            sheet.comment(r + m, cc, "最大耗" + liquidAmmonia + "总数量")
                            value(
                                    sheet,
                                    r + m,
                                    cc++,
                                    (recipe
                                            .maxLiquidAmmoniaWeight
                                    !! * (la2CAUseRatio))
                                            .scale(2),
                                    mergeRow)
                        } else {
                            cc += 4
                        }
                    } else if (ReqData.VITRIOL == matrialName) {
                        if (reqData.isLimitLiquidAmmonia) {
                            val vitriolNormal = recipe.vitriolNormal
                            val vitriolExcess = recipe.vitriolExcess
                            val materialRatio = liquidAmmoniaMaterialRatioMap!![ReqData.VITRIOL]
                            val normal = materialRatio?.normal
                            val originExcess = materialRatio?.originExcess
                            // 耗液氨系数
                            sheet.comment(r + m, cc, "所需硫酸最小耗" + liquidAmmonia + "系数")
                            value(
                                    sheet,
                                    r + m,
                                    cc,
                                    (normal?.min!! * (la2CAUseRatio)).scale(9))
                            if (originExcess != null) {
                                sheet.comment(r + m + 1, cc, "所需过量硫酸最小耗" + liquidAmmonia + "系数")
                                value(
                                        sheet,
                                        r + m + 1,
                                        cc++,
                                        (originExcess
                                                .min
                                        !! * (la2CAUseRatio))
                                                .scale(9))
                            } else {
                                cc++
                            }
                            // 耗液氨数量
                            sheet.comment(r + m, cc, "所需硫酸最小耗" + liquidAmmonia + "数量")
                            value(
                                    sheet,
                                    r + m,
                                    cc,
                                    (vitriolNormal
                                    !! * (normal.min)
                                    !! * (la2CAUseRatio))
                                            .scale(2))
                            if (originExcess != null) {
                                sheet.comment(r + m + 1, cc, "所需过量硫酸最小耗" + liquidAmmonia + "数量")
                                value(
                                        sheet,
                                        r + m + 1,
                                        cc++,
                                        (vitriolExcess
                                        !! * (originExcess.min)
                                        !! * (la2CAUseRatio))
                                                .scale(2))
                            } else {
                                cc++
                            }
                            sheet.comment(r + m, cc, "所需硫酸最大耗" + liquidAmmonia + "系数")
                            value(
                                    sheet,
                                    r + m,
                                    cc,
                                    (normal.max!! * (la2CAUseRatio)).scale(9))
                            if (vitriolExcess != null) {
                                sheet.comment(r + m + 1, cc, "所需过量硫酸最大耗" + liquidAmmonia + "系数")
                                value(
                                        sheet,
                                        r + m + 1,
                                        cc++,
                                        (originExcess
                                                ?.max
                                        !! * (la2CAUseRatio))
                                                .scale(9))
                            } else {
                                cc++
                            }
                            sheet.comment(r + m, cc, "所需硫酸最大耗" + liquidAmmonia + "数量")
                            value(
                                    sheet,
                                    r + m,
                                    cc,
                                    (vitriolNormal * (normal.max)
                                    !! * (la2CAUseRatio))
                                            .scale(2))
                            if (vitriolExcess != null) {
                                sheet.comment(r + m + 1, cc, "所需过量硫酸最大耗" + liquidAmmonia + "数量")
                                value(
                                        sheet,
                                        r + m + 1,
                                        cc++,
                                        (vitriolExcess
                                                * (originExcess!!.max!!)
                                                * (la2CAUseRatio))
                                                .scale(2))
                            } else {
                                cc++
                            }
                        }
                    } else {
                        val limname = if (reqData.isLimitLiquidAmmonia) liquidAmmoniaMaterialRatioMap!!.keys.firstOrNull { materialNameFragment: String? -> ReqData.isNeedLiquidAmmon(materialNameFragment, matrialName) }
                        else null
                        if (limname != null) {
                            val materialRatio = liquidAmmoniaMaterialRatioMap!![limname]
                            val limit = materialRatio?.normal
                            // 耗液氨系数
                            sheet.comment(r + m, cc, limname + "最小耗" + liquidAmmonia + "系数")
                            value(
                                    sheet,
                                    r + m,
                                    cc++,
                                    (limit?.min!! * (la2CAUseRatio)).scale(9),
                                    mergeRow)
                            // 耗液氨数量
                            sheet.comment(r + m, cc, limname + "最小耗" + liquidAmmonia + "数量")
                            value(
                                    sheet,
                                    r + m,
                                    cc++,
                                    (solutionValue
                                    !! * (limit.min)
                                    !! * (la2CAUseRatio))
                                            .scale(2),
                                    mergeRow)

                            // 耗液氨系数
                            sheet.comment(r + m, cc, limname + "最大耗" + liquidAmmonia + "系数")
                            value(
                                    sheet,
                                    r + m,
                                    cc++,
                                    (limit.max!! * (la2CAUseRatio)).scale(9),
                                    mergeRow)
                            // 耗液氨数量
                            sheet.comment(r + m, cc, limname + "最大耗" + liquidAmmonia + "数量")
                            value(
                                    sheet,
                                    r + m,
                                    cc++,
                                    (solutionValue * (limit.max)
                                    !! * (la2CAUseRatio))
                                            .scale(2),
                                    mergeRow)
                        } else {
                            val liquiName = if (reqData.isLimitVitriol) vitriolMaterialRatioMap!!.keys
                                    .firstOrNull { s: String? -> matrialName!!.contains(s!!) }
                            else null
                            if (liquiName != null) {
                                val materialRatio = vitriolMaterialRatioMap!![liquiName]
                                val normal = materialRatio?.normal
                                val excess = materialRatio?.excess
                                // 硫酸系数
                                sheet.comment(r + m, cc, "所需最小硫酸系数")
                                value(sheet, r + m, cc, normal?.min)
                                sheet.comment(r + m + 1, cc, "所需最小过量硫酸系数")
                                value(sheet, r + m + 1, cc++, excess?.min)
                                // 硫酸量
                                sheet.comment(r + m, cc, "所需最小硫酸量")
                                value(
                                        sheet,
                                        r + m,
                                        cc,
                                        (solutionValue!! * (normal!!.min!!)).scale(2))
                                sheet.comment(r + m + 1, cc, "所需最小过量硫酸量")
                                value(
                                        sheet,
                                        r + m + 1,
                                        cc++,
                                        (solutionValue * (excess!!.min)!!).scale(2))
                                // 硫酸系数
                                sheet.comment(r + m, cc, "所需最大硫酸系数")
                                value(sheet, r + m, cc, normal.max)
                                sheet.comment(r + m + 1, cc, "所需最大过量硫酸系数")
                                value(sheet, r + m + 1, cc++, excess.max)
                                // 硫酸量
                                sheet.comment(r + m, cc, "所需最大硫酸量")
                                value(
                                        sheet,
                                        r + m,
                                        cc,
                                        (solutionValue * (normal.max)!!).scale(2))
                                sheet.comment(r + m + 1, cc, "所需最大过量硫酸量")
                                value(
                                        sheet,
                                        r + m + 1,
                                        cc++,
                                        (solutionValue * (excess.max)!!).scale(2))
                            } else {
                                cc += 4
                            }
                        }
                    }

                    // 投料量
                    value(sheet, r + m, cc++, solutionValue!!.scale(2), mergeRow)

                    // 成本
                    val price = material.price!!
                    value(
                            sheet,
                            r + m,
                            cc++,
                            (solutionValue * price / 1000).scale(2),
                            mergeRow)
                    // 单价
                    value(sheet, r + m, cc, price, mergeRow)

                    // 原料成份
                    val components = material.components
                    for (index in components!!.keys) {
                        value(
                                sheet,
                                r + m,
                                c + index, (components[index]
                        !!.value!! * 100.0).scale(2)
                                .toString() + "%",
                                mergeRow)
                    }
                    m++
                    if (mergeRow > 0) {
                        m += mergeRow
                    }
                }
                sheet
                        .range(0, 0, r + m - 1, c + Components.componentNames.size - 1)
                        .style()
                        .borderColor(Color.GRAY7)
                        .borderStyle(BorderStyle.THIN)
                        .set()
            }
            firstSheet!!.keepInActiveTab()
            value(firstSheet, 0, 6, "推优：")
            value(firstSheet, 0, 7, size.toLong())
            if (solveCount > size) {
                value(firstSheet, 0, 3, "计算次数：")
                value(firstSheet, 0, 4, solveCount.toLong())
            }
            value(firstSheet, 1, 6, "计算耗时：")
            value(firstSheet, 1, 7, if (time < 1000) time.toString() + "ms" else (time / 1000).toString() + "s")
            workbook.finish()
            Runtime.getRuntime().exec(arrayOf("xdg-open", outFile.absolutePath))
        }
        System.err.println("==================================================")
        System.err.println("solve times: " + solveCount + " 耗时：" + time + "ms" + " 结果：" + size + "个")
        System.err.println("==================================================")
    }

    companion object {
        // --------------------------------------------
        fun value(sheet: Worksheet?, r: Int, c: Int, value: String?) {
            sheet!!.value(r, c, value)
            style(sheet, r, c, null)
        }

        fun value(sheet: Worksheet, r: Int, c: Int, value: String?, ok: Boolean?) {
            sheet.value(r, c, value)
            style(sheet, r, c, ok)
        }

        fun value(sheet: Worksheet, r: Int, c: Int, value: String?, mergeRow: Int) {
            sheet.value(r, c, value)
            style(sheet, r, c, null)
            if (mergeRow > 0) {
                for (i in 0 until mergeRow) {
                    sheet.value(r + i + 1, c)
                }
                sheet.range(r, c, r + mergeRow, c).merge()
            }
        }

        fun value(sheet: Worksheet, r: Int, c: Int, value: Double?, mergeRow: Int) {
            sheet.value(r, c, value)
            style(sheet, r, c, null)
            if (mergeRow > 0) {
                for (i in 0 until mergeRow) {
                    sheet.value(r + i + 1, c)
                }
                sheet.range(r, c, r + mergeRow, c).merge()
            }
        }

        fun value(sheet: Worksheet, r: Int, c: Int, value: Double?) {
            sheet.value(r, c, value)
            style(sheet, r, c, null)
        }

        fun value(sheet: Worksheet, r: Int, c: Int, value: Long, mergeRow: Int) {
            sheet.value(r, c, value)
            style(sheet, r, c, null)
            if (mergeRow > 0) {
                for (i in 0 until mergeRow) {
                    sheet.value(r + i + 1, c)
                }
                sheet.range(r, c, r + mergeRow, c).merge()
            }
        }

        fun value(sheet: Worksheet, r: Int, c: Int, value: Long?) {
            sheet.value(r, c, value)
            style(sheet, r, c, null)
        }

        private fun style(sheet: Worksheet, r: Int, c: Int, ok: Boolean?) {
            val styleSetter = sheet
                    .style(r, c)
                    .horizontalAlignment(Alignment.CENTER.value)
                    .verticalAlignment(Alignment.CENTER.value)
                    .wrapText(true)
            if (ok != null) {
                styleSetter.fontColor(if (ok) "1fbb7d" else "FF0000")
            }
            styleSetter.set()
        }
    }
}
