package top.bettercode.summer.tools.recipe.result

import top.bettercode.summer.tools.excel.FastExcel
import top.bettercode.summer.tools.optimal.solver.OptimalUtil
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale
import top.bettercode.summer.tools.recipe.RecipeRequirement
import top.bettercode.summer.tools.recipe.indicator.RecipeIndicatorType
import top.bettercode.summer.tools.recipe.material.MaterialCondition

/**
 *
 * @author Peter Wu
 */
object RecipeExport {

    fun FastExcel.exportMaterial(requirement: RecipeRequirement) {
        val materials = requirement.materials.values.toSortedSet()
        val indicators = if (materials.isEmpty()) {
            return
        } else
            materials.first().indicators
        cell(0, 0).value("原料名称").headerStyle().setStyle()
        cell(0, 1).value("价格").headerStyle().setStyle()
        var i = 2
        for (indicator in indicators) {
            val column = i++
            cell(0, column).value(indicator.name).headerStyle().setStyle()
        }
        var r = 0
        for (matrial in materials) {
            val matrialName = matrial.name
            var c = 0
            // 原料名称
            cell(++r, c++).value(matrialName).setStyle()
            // 成本 单价
            cell(r, c++).value(matrial.price * 1000).setStyle()
            // 原料成份
            for (indicator in matrial.indicators) {
                val column = c + indicator.id
                cell(r, column).value(indicator.value.scale()).width(8.0).format("0.0%").setStyle()
            }
        }
    }

    fun FastExcel.exportRequirement(requirement: RecipeRequirement) {

        val rowLen = arrayOf(requirement.notMixMaterials.size, requirement.noUseMaterials.size, requirement.useMaterials.size, requirement.materialIDIndicators.size, requirement.materialIDConstraints.size, requirement.materialConditions.size).maxOrNull()
                ?: 0
        range(0, 0, rowLen, 7).setStyle()

        var r = 0
        var c = 0

        cell(r++, c).value("不能混用约束").headerStyle().width(20.0).setStyle()
        val notMixMaterials = requirement.notMixMaterials
        notMixMaterials.forEach {
            cell(r++, c).value(it.joinToString("和")).width(20.0).wrapText().setStyle()
        }

        c++
        r = 0
        cell(r++, c).value("不使用的原料").headerStyle().width(20.0).setStyle()
        val noUseMaterials = requirement.noUseMaterials
        noUseMaterials.forEach {
            cell(r++, c).value(it).width(20.0).wrapText().setStyle()
        }

        c++
        r = 0
        cell(r++, c).value("限用原料").headerStyle().width(20.0).setStyle()
        val useMaterials = requirement.useMaterials
        useMaterials.forEach {
            cell(r++, c).value(it).width(20.0).wrapText().setStyle()
        }

        c++
        r = 0
        cell(r, c).value("指标限用物料").headerStyle().width(20.0).setStyle()
        cell(r, c + 1).width(20.0).setStyle()
        range(r, c, r++, c + 1).merge()
        val materialIDIndicators = requirement.materialIDIndicators
        materialIDIndicators.forEach { (_, indicator) ->
            cell(r, c).value(indicator.name).width(20.0).wrapText().setStyle()
            cell(r++, c + 1).value(indicator.value.toString()).width(20.0).wrapText().setStyle()
        }

        c += 2
        r = 0
        cell(r, c).value("指定物料约束").headerStyle().width(20.0).setStyle()
        cell(r, c + 1).width(20.0).setStyle()
        range(r, c, r++, c + 1).merge()
        val materialIDConstraints = requirement.materialIDConstraints
        materialIDConstraints.forEach { (materials, limit) ->
            cell(r, c).value(materials.toString()).width(20.0).wrapText().setStyle()
            cell(r++, c + 1).value(limit.toString()).width(20.0).wrapText().setStyle()
        }

        c += 2
        r = 0
        cell(r++, c).value("条件约束").headerStyle().width(20.0).setStyle()
        val materialConditions = requirement.materialConditions
        materialConditions.forEach { (whenCon: MaterialCondition, thenCon: MaterialCondition) ->
            cell(r++, c).value("当" + whenCon.toString() + "时，" + thenCon.toString()).width(20.0).wrapText().setStyle()
        }
    }


    fun FastExcel.exportRecipe(recipe: Recipe) {
        val requirement = recipe.requirement
        RecipeExt(recipe).apply {
            val titles = "最小用量\t最大用量\t最小耗液氨/硫酸系数\t最小耗液氨/硫酸量\t最大耗液氨/硫酸系数\t最大耗液氨/硫酸量\t投料量\t成本\t单价(/吨)".split("\t")
            val materials = recipe.materials.toSortedSet()
            val indicators = materials.first().indicators
            range(0, 0, materials.size + 7, titles.size + indicators.size).setStyle()

            var r = 0
            cell(r, 0).value("配方成本：").width(22.0).setStyle()
            cell(r, 1).value(recipe.cost).bold().format("0.00").width(10.0).setStyle()
            cell(++r, 0).value("产品量：").setStyle()
            cell(r, 1).value(requirement.targetWeight).bold().format("0").setStyle()
            cell(r, 2).value("至少需要哄干的水分：")
            range(r, 2, r, 3).merge().setStyle()
            cell(r, 4).value(recipe.dryWater).bold().format("0.00").width(10.0).setStyle()

            r += 2
            var c = 1
            //标题
            cell(r, 0).headerStyle().setStyle()
            titles.forEach { s ->
                cell(r, c++).value(s).headerStyle().width(if (c in 4..7) 16.0 else 8.0).setStyle()
            }
            if (materials.isEmpty()) {
                return
            }
            indicators.values.forEach { indicator ->
                cell(r, c++).value(indicator.name).headerStyle().width(8.0).setStyle()
            }

            val rangeIndicators = requirement.rangeIndicators
            //成份量 //目标成份量(最大值) //目标成份量(最小值) //成份量(百分比)
            r++
            cell(r++, 0).value("成份量").bold().setStyle()
            cell(r++, 0).value("目标成份量(最大值)").bold().setStyle()
            cell(r++, 0).value("目标成份量(最小值)").bold().setStyle()
            cell(r++, 0).value("成份量(百分比)").bold().setStyle()
            c = titles.size + 1
            indicators.values.forEach { indicator ->
                r = 4
                //成份量
                val indicatorValue = when (indicator.type) {
                    RecipeIndicatorType.WATER -> (materials.sumOf { it.waterWeight } - recipe.dryWater).scale()
                    RecipeIndicatorType.RATE_TO_OTHER -> materials.sumOf { it.indicatorWeight(indicator.itId!!) }.scale()
                    else -> materials.sumOf { it.indicatorWeight(indicator.id) }.scale()
                }
                cell(r++, c).value(indicatorValue).bold().format("0.00").setStyle()

                //目标成份量(最大值)
                val max = rangeIndicators[indicator.id]?.value?.max
                cell(r++, c).value(max).bold().format("0.0%").setStyle()

                //目标成份量(最小值)
                val min = rangeIndicators[indicator.id]?.value?.min
                cell(r++, c).value(min).bold().format("0.0%").setStyle()

                //成份量(百分比)
                val value = when (indicator.type) {
                    RecipeIndicatorType.WATER -> ((materials.sumOf { it.waterWeight } - recipe.dryWater) / requirement.targetWeight).scale()
                    RecipeIndicatorType.RATE_TO_OTHER -> (materials.sumOf { it.indicatorWeight(indicator.itId!!) } / materials.sumOf { it.indicatorWeight(indicator.otherId!!) }).scale()
                    else -> (materials.sumOf { it.indicatorWeight(indicator.id) } / requirement.targetWeight).scale()
                }
                if (min == null || max == null) {
                    cell(r++, c).value(value).bold().format("0.0%").setStyle()
                } else {
                    val valid = value - min >= -OptimalUtil.DEFAULT_MIN_EPSILON && value - max <= OptimalUtil.DEFAULT_MIN_EPSILON
                    cell(r++, c).value(value).bold().format("0.0%").fontColor(if (valid) "1fbb7d" else "FF0000").setStyle()
                }
                c++
            }
            //物料
            val columnSize = titles.size + indicators.size
            materials.forEach { material ->
                c = 0
                //最小用量 最大用量 最小耗液氨/硫酸系数 最小耗液氨/硫酸量 最大耗液氨/硫酸系数 最大耗液氨/硫酸量 投料量 成本 单价(/吨)
                cell(r, c++).value(material.name).wrapText().setStyle()
                val range = material.range
                cell(r, c++).value(range?.min).format("0").setStyle()
                cell(r, c++).value(range?.max).format("0").setStyle()
                val relationRate = material.relationRate
                val normal = relationRate?.normal
                val relationValue = material.relationValue
                val normalValue = relationValue?.first
                val relationName = material.relationName
                cell(r, c++).value(normal?.min).comment(if (normal?.min == null || relationName == null) null else "${material.name}最小耗${relationName}系数").format("0.000000000").setStyle()
                cell(r, c++).value(normalValue?.min).comment(if (normalValue?.min == null || relationName == null) null else "${material.name}最小耗${relationName}数量").format("0.00").setStyle()
                cell(r, c++).value(normal?.max).comment(if (normal?.max == null || relationName == null) null else "${material.name}最大耗${relationName}系数").format("0.000000000").setStyle()
                cell(r, c++).value(normalValue?.max).comment(if (normalValue?.max == null || relationName == null) null else "${material.name}最大耗${relationName}数量").format("0.00").setStyle()
                cell(r, c++).value(material.weight).bold().format("0.00").setStyle()
                cell(r, c++).value(material.cost).format("0.00").setStyle()
                cell(r, c++).value(material.price * 1000).format("0").setStyle()
                material.indicators.forEach { (_, indicator) ->
                    cell(r, c++).value(indicator.value).format("0.0%").setStyle()
                }
                if (material.double) {
                    c = 3
                    val r1 = r + 1
                    val overdose = relationRate?.overdose ?: relationRate?.overdoseMaterial?.normal
                    val overdoseValue = relationValue?.second
                    cell(r1, c++).value(overdose?.min).comment(if (overdose?.min == null || relationName == null) null else "${material.name}过量最小耗${relationName}系数").format("0.000000000").setStyle()
                    cell(r1, c++).value(overdoseValue?.min).comment(if (overdoseValue?.min == null || relationName == null) null else "${material.name}过量最小耗${relationName}数量").format("0.00").setStyle()
                    cell(r1, c++).value(overdose?.max).comment(if (overdose?.max == null || relationName == null) null else "${material.name}过量最大耗${relationName}系数").format("0.000000000").setStyle()
                    cell(r1, c++).value(overdoseValue?.max).comment(if (overdoseValue?.max == null || relationName == null) null else "${material.name}过量最大耗${relationName}数量").format("0.00").setStyle()
                    for (i in 0..columnSize) {
                        if (i !in 3..6) {
                            range(r, i, r1, i).merge()
                        }
                    }
                    r++
                }
                r++
            }
            if (r - 1 > materials.size + 7)
                range(materials.size + 8, 0, r - 1, columnSize).setStyle()
        }
    }


}