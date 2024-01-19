package top.bettercode.summer.tools.recipe.data

import org.dhatim.fastexcel.BorderStyle
import org.dhatim.fastexcel.Color
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import org.springframework.util.StringUtils
import top.bettercode.summer.tools.excel.Alignment
import top.bettercode.summer.tools.excel.ColumnWidths
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale
import top.bettercode.summer.tools.recipe.Recipe
import top.bettercode.summer.tools.recipe.material.IRecipeMaterial
import top.bettercode.summer.tools.recipe.material.MaterialCondition
import top.bettercode.summer.tools.recipe.material.MaterialIDs
import java.io.File
import java.nio.file.Files

/**
 * 配方计算结果
 *
 * @author Peter Wu
 */
class RecipeResult(private val solverName: String) {

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
        // --------------------------------------------
        // 结果输出
        if (recipeCount > 0) {
            val requirement = recipes[0].requirement
            val fileName: String = (requirement.productName
                    + if (requirement.maxUseMaterials <= 0) "配方计算结果-进料口不限" else "配方计算结果-进料口不大于${requirement.maxUseMaterials}")

            val outFile = File(
                    "build/"
                            + solverName
                            + "-${fileName}"
                            + "-推"
                            + recipeCount
                            + "个-"
                            + System.currentTimeMillis()
                            + ".xlsx")
            outFile.parentFile.mkdirs()
            val workbook = Workbook(Files.newOutputStream(outFile.toPath()), "", "1.0")
            var sheet = workbook.newWorksheet("最终候选原料")
            val titles = ("原料名称 价格 " + PrepareData.INDICATOR_NAME_STRING).split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var r = 0
            var c: Int
            for (i in titles.indices) {
                value(sheet, r, i, titles[i])
            }
            val reqMaterials: Collection<IRecipeMaterial> = requirement.materials.values
            for (matrial in reqMaterials) {
                val matrialName = matrial.name
                var cc = 0
                // 原料名称
                value(sheet, ++r, cc++, matrialName)
                // 成本 单价
                value(sheet, r, cc++, matrial.price)
                val indicators = matrial.indicators
                // 原料成份
                for (index in indicators) {
                    value(sheet, r, cc + index.index, index.value.scale(4), "0.0%")
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
                value(sheet, r, 1, recipe.cost.scale(2))
                r++
                value(sheet, r, 0, "产品量：")
                val targetWeight = requirement.targetWeight
                value(sheet, r, 1, targetWeight)
                val dryWater = recipe.dryWater
                value(sheet, r, 2, "至少需要哄干的水分：")
                sheet.range(r, 2, r, 3).merge()
                value(sheet, r, 4, dryWater.scale(2))
                r++
                val notMixMaterials = requirement.notMixMaterials
                if (notMixMaterials.isNotEmpty()) {
                    // 不能混用原料约束
                    value(sheet, r, 0, "不能混用约束：")
                    val str = notMixMaterials.joinToString("，") { arr: Array<MaterialIDs> -> arr.joinToString("和", "(", ")") { it.toString() } }
                    value(sheet, r, 1, str)
                    sheet.style(r, 1).horizontalAlignment(Alignment.LEFT.value).set()
                    sheet.range(r, 1, r, 14).merge()
                }
                r++
                value(sheet, r, 0, "不使用的原料：")
                val notUseMaterialNames = requirement.noUseMaterials
                value(sheet, r, 1, StringUtils.collectionToDelimitedString(notUseMaterialNames, "，"))
                sheet.style(r, 1).horizontalAlignment(Alignment.LEFT.value).set()
                sheet.range(r, 1, r, 14).merge()
                r++
                value(sheet, r, 0, "约束：")
                val limitMaterialStrings: MutableList<String?> = ArrayList()
                val rangeIndicators = requirement.rangeIndicators
                val materialIDIndicators = requirement.materialIDIndicators
                val componentNames: List<String> = PrepareData.indicatorNames
                for (index in materialIDIndicators.values) {
                    val limit = index.value
                    limitMaterialStrings.add(index.name + "只能用：" + limit)
                }
                val materialRangeConstraints = requirement.materialRangeConstraints
                val materialIDConstraints = requirement.materialIDConstraints
                materialIDConstraints
                        .forEach { (name: MaterialIDs, limit: MaterialIDs) ->
                            limitMaterialStrings.add(name.toString() + "只能用：" + limit)
                        }
                requirement
                        .materialConditions
                        .forEach { (condition1: MaterialCondition, condition2: MaterialCondition) ->
                            limitMaterialStrings.add(
                                    "当" + condition1.toString() + "时，" + condition2.toString())
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
                var liquidAmmonia: String = LIQUID_AMMONIA
                var la2CAUseRatio = 1.0
                val materials = recipe.materials
                if (!materials.any { it.name == liquidAmmonia }) {
                    la2CAUseRatio = LA_2_CAUSE_RATIO
                    liquidAmmonia = CLIQUID_AMMONIA
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
                for (index in rangeIndicators) {
                    r = row
                    val c1 = c + index.index
                    var weight = materials.sumOf { m -> m.indicatorWeight(index.index) }
                    if (index.isWater) {
                        weight -= dryWater
                    } else if (index.isRateToOther) {
                        weight = materials.sumOf { m -> m.indicatorWeight(index.itIndex!!) }
                    }
                    value(sheet, r++, c1, weight.scale(2))

                    val limit = index.value
                    value(sheet, r++, c1, limit.max.scale(4), "0.0%")
                    value(sheet, r++, c1, limit.min.scale(4), "0.0%")
                    var v = (weight / targetWeight).scale(4)
                    if (index.isRateToOther)
                        v = (weight / materials.sumOf { m -> m.indicatorWeight(index.otherIndex!!) }).scale(4)
                    val valid = v >= limit.min && v <= limit.max
                    value(sheet, r++, c1, v.scale(4), "0.0%", valid)
                }
                var m = 0
                val vitriolMaterialRatio = requirement.sulfuricAcidRelation
                val liquidAmmoniaMaterialRatio = requirement.liquidAmmoniaRelation
                val isLimitVitriol = vitriolMaterialRatio != null
                val isLimitLiquidAmmonia = liquidAmmoniaMaterialRatio != null
                for (material in materials) {
                    val id = material.id
                    val solutionValue = material.solutionValue.value
                    var cc = 0
                    var mergeRow = 0
                    if (VITRIOL == id && isLimitLiquidAmmonia) {
                        mergeRow = 1
                    } else {
                        if (isLimitVitriol) {
                            for (mid in vitriolMaterialRatio!!.keys) {
                                if (mid.contains(id)) {
                                    mergeRow = 1
                                    break
                                }
                            }
                        }
                    }

                    // 原料名称
                    value(sheet, r + m, cc++, id, mergeRow)
                    if (materialRangeConstraints.isNotEmpty()) {
                        val firstOrNull = materialRangeConstraints.filter { it.key.contains(id) }.values.firstOrNull()
                        if (firstOrNull != null) {
                            // 最小用量
                            value(sheet, r + m, cc++, firstOrNull.min, mergeRow)
                            // 最大用量
                            value(sheet, r + m, cc++, firstOrNull.max, mergeRow)
                        } else {
                            cc += 2
                        }
                    } else {
                        cc += 2
                    }
                    if (liquidAmmonia == id) {
                        cc += 4
                    } else if (VITRIOL == id) {
                        if (isLimitLiquidAmmonia) {
                            val vsolutionValue = material.solutionValue
                            val vitriolNormal = vsolutionValue.normal ?: vsolutionValue.value
                            val vitriolExcess = vsolutionValue.overdose
                            val relationPair = liquidAmmoniaMaterialRatio!![MaterialIDs.of(id)]
                            val normal = relationPair?.normal
                            val originExcess = relationPair?.overdose
                            // 耗液氨系数
                            sheet.comment(r + m, cc, "所需硫酸最小耗" + liquidAmmonia + "系数")
                            value(
                                    sheet,
                                    r + m,
                                    cc,
                                    (normal?.min!!).scale(9))
                            if (originExcess != null) {
                                sheet.comment(r + m + 1, cc, "所需过量硫酸最小耗" + liquidAmmonia + "系数")
                                value(
                                        sheet,
                                        r + m + 1,
                                        cc++,
                                        (originExcess
                                                .min)
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
                                    (vitriolNormal * (normal.min))
                                            .scale(2))
                            if (originExcess != null && vitriolExcess != null) {
                                sheet.comment(r + m + 1, cc, "所需过量硫酸最小耗" + liquidAmmonia + "数量")
                                value(
                                        sheet,
                                        r + m + 1,
                                        cc++,
                                        (vitriolExcess * (originExcess.min))
                                                .scale(2))
                            } else {
                                cc++
                            }
                            sheet.comment(r + m, cc, "所需硫酸最大耗" + liquidAmmonia + "系数")
                            value(
                                    sheet,
                                    r + m,
                                    cc,
                                    (normal.max).scale(9))
                            if (originExcess != null) {
                                sheet.comment(r + m + 1, cc, "所需过量硫酸最大耗" + liquidAmmonia + "系数")
                                value(
                                        sheet,
                                        r + m + 1,
                                        cc++,
                                        (originExcess.max)
                                                .scale(9))
                            } else {
                                cc++
                            }
                            sheet.comment(r + m, cc, "所需硫酸最大耗" + liquidAmmonia + "数量")
                            value(
                                    sheet,
                                    r + m,
                                    cc,
                                    (vitriolNormal * (normal.max))
                                            .scale(2))
                            if (originExcess != null) {
                                sheet.comment(r + m + 1, cc, "所需过量硫酸最大耗" + liquidAmmonia + "数量")
                                value(
                                        sheet,
                                        r + m + 1,
                                        cc++,
                                        (vitriolExcess!!
                                                * (originExcess.max)
                                                )
                                                .scale(2))
                            } else {
                                cc++
                            }
                        } else {
                            cc += 4
                        }
                    } else {
                        val liqRelationPair = if (isLimitLiquidAmmonia) liquidAmmoniaMaterialRatio!!.filter { it.key.contains(id) }.values.firstOrNull()
                        else null
                        if (liqRelationPair != null) {
                            val limit = liqRelationPair.normal
                            // 耗液氨系数
                            sheet.comment(r + m, cc, id + "最小耗" + liquidAmmonia + "系数")
                            value(
                                    sheet,
                                    r + m,
                                    cc++,
                                    (limit.min * (la2CAUseRatio)).scale(9),
                                    mergeRow)
                            // 耗液氨数量
                            sheet.comment(r + m, cc, id + "最小耗" + liquidAmmonia + "数量")
                            value(
                                    sheet,
                                    r + m,
                                    cc++,
                                    (solutionValue * (limit.min) * (la2CAUseRatio))
                                            .scale(2),
                                    mergeRow)

                            // 耗液氨系数
                            sheet.comment(r + m, cc, id + "最大耗" + liquidAmmonia + "系数")
                            value(
                                    sheet,
                                    r + m,
                                    cc++,
                                    (limit.max * (la2CAUseRatio)).scale(9),
                                    mergeRow)
                            // 耗液氨数量
                            sheet.comment(r + m, cc, id + "最大耗" + liquidAmmonia + "数量")
                            value(
                                    sheet,
                                    r + m,
                                    cc++,
                                    (solutionValue * (limit.max) * (la2CAUseRatio))
                                            .scale(2),
                                    mergeRow)
                        } else {
                            val relationPair = if (isLimitVitriol) vitriolMaterialRatio!!.filter { it.key.contains(id) }.values.firstOrNull()
                            else null
                            if (relationPair != null) {
                                val normal = relationPair.normal
                                val excess = relationPair.overdose
                                // 硫酸系数
                                sheet.comment(r + m, cc, "所需最小硫酸系数")
                                value(sheet, r + m, cc, normal.min)
                                sheet.comment(r + m + 1, cc, "所需最小过量硫酸系数")
                                value(sheet, r + m + 1, cc++, excess?.min)
                                // 硫酸量
                                sheet.comment(r + m, cc, "所需最小硫酸量")
                                value(
                                        sheet,
                                        r + m,
                                        cc,
                                        (solutionValue * (normal.min)).scale(2))
                                sheet.comment(r + m + 1, cc, "所需最小过量硫酸量")
                                value(
                                        sheet,
                                        r + m + 1,
                                        cc++,
                                        (solutionValue * (excess!!.min)).scale(2))
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
                                        (solutionValue * (normal.max)).scale(2))
                                sheet.comment(r + m + 1, cc, "所需最大过量硫酸量")
                                value(
                                        sheet,
                                        r + m + 1,
                                        cc++,
                                        (solutionValue * (excess.max)).scale(2))
                            } else {
                                cc += 4
                            }
                        }
                    }

                    // 投料量
                    value(sheet, r + m, cc++, solutionValue.scale(2), mergeRow)

                    // 成本
                    val price = material.price
                    value(
                            sheet,
                            r + m,
                            cc++,
                            (solutionValue * price / 1000).scale(2),
                            mergeRow)
                    // 单价
                    value(sheet, r + m, cc, price, mergeRow)

                    // 原料成份
                    val components = material.indicators
                    for (index in components) {
                        value(sheet, r + m, c + index.index, index.value.scale(4), mergeRow, "0.0%")
                    }
                    m++
                    if (mergeRow > 0) {
                        m += mergeRow
                    }
                }
                sheet
                        .range(0, 0, r + m - 1, c + PrepareData.indicatorNames.size - 1)
                        .style()
                        .borderColor(Color.GRAY7)
                        .borderStyle(BorderStyle.THIN)
                        .set()
            }
            firstSheet!!.keepInActiveTab()
            value(firstSheet, 0, 6, "推优：")
            value(firstSheet, 0, 7, recipeCount.toLong())
            if (solveCount > recipeCount) {
                value(firstSheet, 0, 3, "计算次数：")
                value(firstSheet, 0, 4, solveCount.toLong())
            }
            value(firstSheet, 1, 6, "计算耗时：")
            value(firstSheet, 1, 7, if (time < 1000) time.toString() + "ms" else (time / 1000).toString() + "s")
            workbook.finish()
            Runtime.getRuntime().exec(arrayOf("xdg-open", outFile.absolutePath))
        }
        System.err.println("==================================================")
        System.err.println("solve times: " + solveCount + " 耗时：" + time + "ms" + " 结果：" + recipeCount + "个")
        System.err.println("==================================================")
    }

    companion object {
        /** 碳铵原料名称  */
        const val CLIQUID_AMMONIA = "碳铵"

        /** 液氨原料名称  */
        const val LIQUID_AMMONIA = "液氨"

        /** 硫酸原料名称  */
        const val VITRIOL = "硫酸"

        /** 液氨 对应 碳铵 使用量比例  */
        const val LA_2_CAUSE_RATIO = 4.7647

        fun isNeedLiquidAmmon(materialNameFragment: String?, materialName: String?): Boolean {
            val needLiquidAmmon: Boolean = when (materialNameFragment) {
                "硫酸" -> {
                    materialName == materialNameFragment
                }

                "磷酸" -> {
                    materialName == materialNameFragment
                }

                else -> {
                    materialName!!.contains(materialNameFragment!!)
                }
            }
            return needLiquidAmmon
        }

        // --------------------------------------------
        fun value(sheet: Worksheet?, r: Int, c: Int, value: String?) {
            sheet!!.value(r, c, value)
            style(sheet, r, c, null)
        }

        fun value(sheet: Worksheet, r: Int, c: Int, value: Double?, mergeRow: Int, format: String? = null) {
            sheet.value(r, c, value)
            style(sheet, r, c, null, format)
            if (mergeRow > 0) {
                for (i in 0 until mergeRow) {
                    sheet.value(r + i + 1, c)
                }
                sheet.range(r, c, r + mergeRow, c).merge()
            }
        }

        fun value(sheet: Worksheet, r: Int, c: Int, value: String?, mergeRow: Int, format: String? = null) {
            sheet.value(r, c, value)
            style(sheet, r, c, null, format)
            if (mergeRow > 0) {
                for (i in 0 until mergeRow) {
                    sheet.value(r + i + 1, c)
                }
                sheet.range(r, c, r + mergeRow, c).merge()
            }
        }

        fun value(sheet: Worksheet, r: Int, c: Int, value: Double?, format: String? = null, ok: Boolean? = null) {
            sheet.value(r, c, value)
            style(sheet, r, c, ok, format)
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

        private fun style(sheet: Worksheet, r: Int, c: Int, ok: Boolean?, format: String? = null) {
            val styleSetter = sheet
                    .style(r, c)
                    .horizontalAlignment(Alignment.CENTER.value)
                    .verticalAlignment(Alignment.CENTER.value)
                    .format(format)
                    .wrapText(true)
            if (ok != null) {
                styleSetter.fontColor(if (ok) "1fbb7d" else "FF0000")
            }
            styleSetter.set()
        }
    }
}
