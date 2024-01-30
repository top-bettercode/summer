package top.bettercode.summer.tools.recipe.result

import top.bettercode.summer.tools.excel.FastExcel
import top.bettercode.summer.tools.optimal.solver.OptimalUtil
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale
import top.bettercode.summer.tools.recipe.RecipeRequirement
import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.criteria.RecipeRelation
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
            matrial.indicators.values.forEachIndexed { index, indicator ->
                val column = c + index
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


    fun FastExcel.exportRecipe(recipe: Recipe, showRate: Boolean = false) {
        val requirement = recipe.requirement
        RecipeExt(recipe).apply {
            val titles = "项目${if (showRate) "\t最小耗液氨/硫酸系数\t最小耗液氨/硫酸量\t最大耗液氨/硫酸系数\t最大耗液氨/硫酸量" else ""}\t投料量".split("\t")
            val materials = recipe.materials.toSortedSet()
            val rangeIndicators = requirement.rangeIndicators.values.sortedBy { it.index }
            val limitMaterials = materials.filter { it.range != null }
            val columnSize = titles.size + rangeIndicators.size + limitMaterials.size
            range(0, 0, materials.size + 3, columnSize).setStyle()

            var r = 0
            var c = 0
            //标题
            titles.forEach { s ->
                cell(r, c++).value(s).headerStyle().width(if (c in 1..5) 16.0 else 8.0).setStyle()
            }
            if (materials.isEmpty()) {
                return
            }
            rangeIndicators.forEach { indicator ->
                cell(r, c++).value(indicator.name).headerStyle().width(8.0).setStyle()
            }

            r++
            cell(r++, 0).value("配方目标最大值").bold().setStyle()
            cell(r++, 0).value("配方目标最小值").bold().setStyle()
            cell(r++, 0).value("实配值").bold().setStyle()
            c = titles.size
            // 投料量
            for (i in 1..2) {
                cell(i, c - 1).value("/").setStyle()
            }
            cell(3, c - 1).value(recipe.weight).bold().format("0.00").setStyle()
            rangeIndicators.forEach { indicator ->
                r = 1
                //配方目标最大值
                val max = indicator.value.max
                cell(r++, c).value(max).bold().format("0.0%").setStyle()

                //配方目标最小值
                val min = indicator.value.min
                cell(r++, c).value(min).bold().format("0.0%").setStyle()

                //实配值
                val value = when (indicator.type) {
                    RecipeIndicatorType.PRODUCT_WATER -> ((materials.sumOf { it.waterWeight } - recipe.dryWater) / requirement.targetWeight).scale()
                    RecipeIndicatorType.RATE_TO_OTHER -> (materials.sumOf { it.indicatorWeight(indicator.itId!!) } / materials.sumOf { it.indicatorWeight(indicator.otherId!!) }).scale()
                    else -> (materials.sumOf { it.indicatorWeight(indicator.id) } / requirement.targetWeight).scale()
                }
                val valid = value - min >= -OptimalUtil.DEFAULT_MIN_EPSILON && value - max <= OptimalUtil.DEFAULT_MIN_EPSILON
                cell(r++, c).value(value).bold().format("0.0%").fontColor(if (valid) "1fbb7d" else "FF0000").setStyle()
                c++
            }
            // 物料限量
            limitMaterials.forEach {
                r = 0
                cell(r++, c).value(it.name + "用量").headerStyle().width(8.0).setStyle()
                cell(r++, c).value(it.range!!.max).bold().format("0").setStyle()
                cell(r++, c).value(it.range!!.min).bold().format("0").setStyle()
                val valid = it.weight - it.range!!.min >= -OptimalUtil.DEFAULT_MIN_EPSILON && it.weight - it.range!!.max <= OptimalUtil.DEFAULT_MIN_EPSILON
                cell(r++, c++).value(it.weight).bold().format("0").fontColor(if (valid) "1fbb7d" else "FF0000").setStyle()
            }
            // 费用合计
            cell(0, c).value("费用合计").headerStyle().width(8.0).setStyle()
            for (i in 1..2) {
                cell(i, c).value("/").setStyle()
            }
            cell(3, c).value(recipe.cost).bold().format("0.00").setStyle()
            //物料
            materials.forEach { material ->
                c = 0
                cell(r, c++).value(material.name).wrapText().setStyle()
                val relationRate: RecipeRelation?
                val normal: DoubleRange?
                val relationValue: Pair<DoubleRange, DoubleRange>?
                val normalValue: DoubleRange?
                val relationName: String?
                if (showRate) {
                    relationRate = material.relationRate
                    normal = relationRate?.normal
                    relationValue = material.relationValue
                    normalValue = relationValue?.first
                    relationName = material.relationName
                    // 最小耗液氨/硫酸系数
                    cell(r, c++).value(normal?.min).comment(if (normal?.min == null || relationName == null) null else "${material.name}最小耗${relationName}系数").format("0.000000000").setStyle()
                    // 最小耗液氨/硫酸量
                    cell(r, c++).value(normalValue?.min).comment(if (normalValue?.min == null || relationName == null) null else "${material.name}最小耗${relationName}数量").format("0.00").setStyle()
                    // 最大耗液氨/硫酸系数
                    cell(r, c++).value(normal?.max).comment(if (normal?.max == null || relationName == null) null else "${material.name}最大耗${relationName}系数").format("0.000000000").setStyle()
                    // 最大耗液氨/硫酸量
                    cell(r, c++).value(normalValue?.max).comment(if (normalValue?.max == null || relationName == null) null else "${material.name}最大耗${relationName}数量").format("0.00").setStyle()
                } else {
                    relationRate = null
                    relationValue = null
                    relationName = null
                }
                // 投料量
                cell(r, c++).value(material.weight).bold().format("0.00").setStyle()
                rangeIndicators.forEach { indicator ->
                    val value = when (indicator.type) {
                        RecipeIndicatorType.PRODUCT_WATER -> material.indicators.waterValue
                        else -> material.indicators[indicator.id]?.value
                    }
                    cell(r, c++).value(value).format("0.0%").setStyle()
                }
                limitMaterials.forEach { _ ->
                    cell(r, c++).value("/").setStyle()
                }
                // 费用合计
                cell(r, c++).value(material.cost).format("0.00").setStyle()
                if (showRate && material.double) {
                    c = 1
                    val r1 = r + 1
                    val overdose = relationRate?.overdose ?: relationRate?.overdoseMaterial?.normal
                    val overdoseValue = relationValue?.second
                    // 最小耗液氨/硫酸系数
                    cell(r1, c++).value(overdose?.min).comment(if (overdose?.min == null || relationName == null) null else "${material.name}过量最小耗${relationName}系数").format("0.000000000").setStyle()
                    // 最小耗液氨/硫酸量
                    cell(r1, c++).value(overdoseValue?.min).comment(if (overdoseValue?.min == null || relationName == null) null else "${material.name}过量最小耗${relationName}数量").format("0.00").setStyle()
                    // 最大耗液氨/硫酸系数
                    cell(r1, c++).value(overdose?.max).comment(if (overdose?.max == null || relationName == null) null else "${material.name}过量最大耗${relationName}系数").format("0.000000000").setStyle()
                    // 最大耗液氨/硫酸量
                    cell(r1, c++).value(overdoseValue?.max).comment(if (overdoseValue?.max == null || relationName == null) null else "${material.name}过量最大耗${relationName}数量").format("0.00").setStyle()
                    for (i in 0..columnSize) {
                        if (i !in 1..4) {
                            range(r, i, r1, i).merge().setStyle()
                        }
                    }
                    r++
                }
                r++
            }
        }
    }


}