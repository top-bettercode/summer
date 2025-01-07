package top.bettercode.summer.tools.recipe.result

import top.bettercode.summer.tools.excel.Excel
import top.bettercode.summer.tools.optimal.OptimalUtil.scale
import top.bettercode.summer.tools.recipe.RecipeRequirement
import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.criteria.RecipeRelation
import top.bettercode.summer.tools.recipe.indicator.IndicatorUnit
import top.bettercode.summer.tools.recipe.indicator.RecipeIndicatorType
import top.bettercode.summer.tools.recipe.indicator.RecipeIndicatorValue
import top.bettercode.summer.tools.recipe.material.RecipeMaterial
import top.bettercode.summer.tools.recipe.productioncost.ChangeLogicType
import kotlin.math.absoluteValue
import kotlin.math.max

/**
 *
 * @author Peter Wu
 */
object RecipeExport {

    fun Excel.exportMaterial(requirement: RecipeRequirement) {
        val materials =
            requirement.materials.sortedWith(Comparator.comparing<RecipeMaterial?, Boolean?> { it in requirement.nonUseMaterials }
                .thenBy { it.id.toIntOrNull() ?: Int.MAX_VALUE }.thenBy { it.id })
        val indicators = if (materials.isEmpty()) {
            return
        } else
            requirement.indicators.filter { !it.isProductWater }.sortedBy { it.index }
        //标题
        cell(0, 0).value("原料名称").headerStyle().setStyle()
        cell(0, 1).value("价格").headerStyle().setStyle()
        var i = 2
        for (indicator in indicators) {
            val column = i++
            cell(0, column).value("${indicator.name}(${indicator.unit})").headerStyle().width(11.0)
                .setStyle()
        }
        //原料
        var r = 0
        for (matrial in materials) {
            val matrialName = "$matrial"
            var c = 0
            // 原料名称
            cell(++r, c++).value(matrialName)
                .comment(if (matrial in requirement.nonUseMaterials) "未使用" else "").setStyle()
            // 成本 单价
            cell(r, c++).value(matrial.price * 1000).setStyle()
            // 原料成份
            indicators.sortedBy { it.index }
                .forEachIndexed { index, indicator ->
                    val column = c + index
                    val value =
                        if (indicator.isTotalNutrient) matrial.totalNutrient else matrial.indicators.valueOf(
                            indicator.id
                        )

                    cell(r, column).value(value)
                        .format(
                            if (IndicatorUnit.PERCENTAGE.eq(indicator.unit)) requirement.indicatorFormat else ""
                        )
                        .setStyle()
                }
        }
    }

    fun Excel.exportRequirement(requirement: RecipeRequirement) {
        requirement.apply {
            val startCol = 0
            val startRow = 0
            val rangeIndicators = indicatorRangeConstraints.values.sortedBy { it.indicator.index }
            val productionColumnSize =
                productionCost.materialItems.size + productionCost.dictItems.size + 2
            val columnSize = max(rangeIndicators.size, productionColumnSize) + startCol
            var c = startCol
            var r = startRow
            cell(r++, c).value("项目").headerStyle().width(20.0).setStyle()
            cell(r++, c).value("配方目标最大值").setStyle()
            cell(r, c).value("配方目标最小值").setStyle()
            //配方目标
            c++
            rangeIndicators.forEach {
                val indicator = it.indicator
                r = startCol
                val unit = indicator.unit
                cell(r++, c).value("${indicator.name}($unit)").headerStyle().width(11.0).setStyle()
                cell(r++, c).value(it.scaledValue.max)
                    .format(if (unit == IndicatorUnit.PERCENTAGE.unit) requirement.indicatorFormat else "")
                    .setStyle()
                cell(r++, c++).value(it.scaledValue.min)
                    .format(if (unit == IndicatorUnit.PERCENTAGE.unit) requirement.indicatorFormat else "")
                    .setStyle()
            }
            if (columnSize > c)
                range(startRow, c, r - 1, columnSize).merge().horizontalAlignment("left").setStyle()
            //推优原料限制
            c = startCol
            cell(r, c++).value("推优原料限制").height(20.0).setStyle()
            //保留用原料
            val keepMaterials = keepMaterialConstraints
            //不能用原料
            val noUseMaterials = noUseMaterialConstraints
            cell(
                r,
                c
            ).value(
                "保留用原料：${keepMaterials.toNames(requirement)}；不能用原料：${
                    noUseMaterials.toNames(
                        requirement
                    )
                }"
            )
            range(r, c, r++, columnSize).merge().horizontalAlignment("left").setStyle()
            //推优原料用量范围
            c = startCol
            val limitMaterials = materialRangeConstraints
            cell(r, c++).value("推优原料用量范围").height(20.0 * limitMaterials.size).setStyle()
            val limitMaterialsStr = limitMaterials.joinToString("\n") {
                "${it.term.toNames(requirement)} 用量范围：${it.then} 公斤；"
            }
            cell(r, c).value(limitMaterialsStr)
            range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText().setStyle()
            // 推优原料用量限制
            c = startCol
            val materialConditionConstraints = materialConditionConstraints
            cell(r, c++).value("推优原料用量范围").height(20.0 * materialConditionConstraints.size)
                .setStyle()
            val materialConditionConstraintsStr = materialConditionConstraints.joinToString("\n") {
                "如果 ${it.term.materials.toNames(requirement)} ${it.term.condition}公斤时，${
                    it.then.materials.toNames(
                        requirement
                    )
                } ${it.then.condition}公斤；"
            }
            cell(r, c).value(materialConditionConstraintsStr)
            range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText().setStyle()

            // 硫酸/液氨/碳铵计算规则
            c = startCol
            val relationConstraints = materialRelationConstraints
            cell(r, c++).value("硫酸/液氨/碳铵计算规则")
                .height(40.0 * relationConstraints.sumOf { it.then.size }).setStyle()
            val relationConstraintsStr = relationConstraints.joinToString("\n\n") {
                "启用${it.term.toNames(requirement)}计算规则${
                    if (it.term.replaceIds == null) "\n" else " ${
                        it.term.toNames(
                            requirement
                        )
                    }/${it.term.replaceIds.toNames(requirement)}用量换算系数：${it.term.replaceRate ?: "未设置"}\n"
                }${
                    it.then.joinToString("\n") { v ->
                        val normal = v.then.normal
                        val overdose = v.then.overdose
                        val overdoseMaterial = v.then.overdoseMaterial
                        val overdoseMaterialNormal = overdoseMaterial?.normal
                        val overdoseMaterialOverdose = overdoseMaterial?.overdose
                        "${v.term.relationIds?.toNames(requirement) ?: ""}使用 ${
                            v.term.toNames(
                                requirement
                            )
                        } 时${if (normal != null) "，耗${it.term.toNames(requirement)}系数：${normal.min}-${normal.max}" else ""}${
                            if (overdose != null) {
                                "\n${v.term.relationIds?.toNames(requirement) ?: ""}使用 ${
                                    v.term.toNames(
                                        requirement
                                    )
                                } 时，过量耗${it.term.toNames(requirement)}系数：${overdose.min}-${overdose.max}"
                            } else {
                                ""
                            }
                        }${
                            if (overdoseMaterialNormal != null) {
                                "\n${v.term.relationIds?.toNames(requirement) ?: ""}使用过量 ${
                                    v.term.toNames(
                                        requirement
                                    )
                                } 时，耗${it.term.toNames(requirement)}系数：${overdoseMaterialNormal.min}-${overdoseMaterialNormal.max}"
                            } else {
                                ""
                            }
                        }${
                            if (overdoseMaterialOverdose != null) {
                                "\n${v.term.relationIds?.toNames(requirement) ?: ""}使用过量 ${
                                    v.term.toNames(
                                        requirement
                                    )
                                } 时，过量耗${it.term.toNames(requirement)}系数：${overdoseMaterialOverdose.min}-${overdoseMaterialOverdose.max}"
                            } else {
                                ""
                            }
                        }"
                    }
                }"
            }
            cell(r, c).value(relationConstraintsStr)
            range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText().setStyle()

            //标准制造费用
            val productionCost = productionCost
            c = startCol
            val pr = r
            cell(r, c).value("标准制造费用").setStyle()
            range(r, c, r + 3, c++).merge().setStyle()
            cell(r++, c).value("费用项目").setStyle()
            cell(r++, c).value("单价").setStyle()
            cell(r++, c).value("数量").setStyle()
            cell(r++, c++).value("合计").setStyle()
            productionCost.materialItems.forEach {
                r = pr
                cell(
                    r++,
                    c
                ).value("${it}${if (it.unit.isBlank()) "" else "(${it.unit})"}")
                    .headerStyle().setStyle()
                cell(r++, c).value(it.price).format("0.00").setStyle()
                cell(r++, c).value(it.value).format("0.00").setStyle()
                cell(r++, c++).value(it.cost).format("0.00").setStyle()
            }
            productionCost.dictItems.forEach {
                r = pr
                cell(r++, c).value(it.key.dictName).headerStyle().setStyle()
                cell(r++, c).value(it.value.price).format("0.00").setStyle()
                cell(r++, c).value(it.value.value).format("0.00").setStyle()
                cell(r++, c++).value(it.value.cost).format("0.00").setStyle()
            }
            r = pr
            cell(r++, c).value("税费").headerStyle().setStyle()
            cell(r++, c).value(productionCost.taxRate).comment("税费税率").format("0.00").setStyle()
            cell(r++, c).value(productionCost.taxFloat).comment("税费浮动值").format("0.00")
                .setStyle()
            cell(
                r++,
                c
            ).value(productionCost.dictItems.values.sumOf { it.cost } * productionCost.taxRate + productionCost.taxFloat)
                .comment("税费").format("0.00").setStyle()
            if (c < columnSize)
                range(pr, c + 1, r - 1, columnSize).merge().setStyle()

            //制造费用增减逻辑
            val changes = productionCost.changes
            c = startCol
            cell(r, c++).value("制造费用增减逻辑").height(20.0 * (changes.size + 1)).setStyle()
            val changesStr = changes.joinToString("\n") { logic ->
                when (logic.type) {
                    ChangeLogicType.WATER_OVER -> {
                        val filter =
                            materials.filter { m -> logic.materialId?.contains(m.id) == true }
                        val name =
                            if (filter.isNotEmpty()) filter.joinToString { "$it" } else logic.materialId?.joinToString()
                        "当使用${name}产肥一吨总水分超过${logic.exceedValue}公斤后，每${if (logic.eachValue != null && logic.eachValue >= 0) "增加" else "减少"}${logic.eachValue?.absoluteValue}公斤，${
                            logic.changeItems?.joinToString { item ->
                                item.toName(productionCost)
                            }
                        }${if (logic.changeValue >= 0) "增加" else "减少"}${logic.changeValue.absoluteValue * 100}%"
                    }

                    ChangeLogicType.OVER -> {
                        val filter =
                            materials.filter { m -> logic.materialId?.contains(m.id) == true }
                        val name =
                            if (filter.isNotEmpty()) filter.joinToString { "$it" } else logic.materialId?.joinToString()
                        "当产肥一吨使用${name}超过${logic.exceedValue}公斤后，每${if (logic.eachValue != null && logic.eachValue >= 0) "增加" else "减少"}${logic.eachValue?.absoluteValue}公斤，${
                            logic.changeItems?.joinToString { item ->
                                item.toName(productionCost)
                            }
                        }${if (logic.changeValue >= 0) "增加" else "减少"}${logic.changeValue.absoluteValue * 100}%"
                    }

                    ChangeLogicType.OTHER -> "其他额外${if (logic.changeValue >= 0) "增加" else "减少"}制造费用${logic.changeValue.absoluteValue * 100}%"
                }
            }
            cell(r, c).value(changesStr)
            range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText().setStyle()

            //指标指定用原料
            val materialIDIndicators = indicatorMaterialIDConstraints
            if (materialIDIndicators.isNotEmpty()) {
                c = startCol
                cell(r, c++).value("指标指定用原料").height(20.0 * materialIDIndicators.size)
                    .setStyle()
                val materialIDIndicatorsStr =
                    materialIDIndicators.entries.joinToString("\n") {
                        "指标${it.value.indicator.name} 限用原料：${
                            it.value.value.toNames(
                                requirement
                            )
                        }"
                    }
                cell(r, c).value(materialIDIndicatorsStr)
                range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText()
                    .setStyle()
            }
            // 不能混用的原料
            val notMixMaterialConstraints = notMixMaterialConstraints
            if (notMixMaterialConstraints.isNotEmpty()) {
                c = startCol
                cell(r, c++).value("不能混用的原料").height(20.0 * notMixMaterialConstraints.size)
                    .setStyle()
                val notMixMaterialConstraintsStr =
                    notMixMaterialConstraints.joinToString("\n") { notMix ->
                        notMix.joinToString("和") { it.toNames(requirement) } + "不能混用"
                    }
                cell(r, c).value(notMixMaterialConstraintsStr)
                range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText()
                    .setStyle()
            }
            // 指定原料约束
            val materialIDConstraints = materialIDConstraints
            if (materialIDConstraints.isNotEmpty()) {
                c = startCol
                cell(r, c++).value("指定原料约束").height(20.0 * materialIDConstraints.size)
                    .setStyle()
                val materialIDConstraintsStr = materialIDConstraints.joinToString("\n") {
                    "${it.term.toNames(requirement)}中指定使用原料：${it.then.toNames(requirement)}"
                }
                cell(r, c).value(materialIDConstraintsStr)
                range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText()
                    .setStyle()
            }

            //其它产线信息
            c = startCol
            cell(r, c++).value("其它产线信息").height(20.0).setStyle()
            cell(
                r,
                c
            ).value("可用原料种类最大数：${maxUseMaterialNum ?: "不限"}；单吨产品水份最大烘干量：${if (maxBakeWeight == null) "不限" else "${maxBakeWeight}公斤"}；收率：${this.yield * 100}%")
            range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText().setStyle()
        }
    }

    fun Excel.exportProductionCost(recipe: Recipe, row: Int = 0) {
        recipe.productionCost.apply {
            var r = row
            var c = 0
            cell(r++, c).value("费用项目").headerStyle().width(20.0).setStyle()
            cell(r++, c).value("单价").setStyle()
            cell(r++, c).value("数量").setStyle()
            cell(r++, c).value("增减").setStyle()
            cell(r++, c).value("合计").setStyle()
            cell(r++, c).value("税费").setStyle()
            cell(r++, c).value("制造费用（元）(${(allChange * 100).scale(2)}%)").bold().setStyle()
            cell(r++, c).value("原辅料成本（元）").bold().setStyle()
            cell(r++, c).value("原辅料成本（元）/收率").bold().setStyle()
            cell(r++, c).value("包装费用（元）").bold().setStyle()
            cell(r++, c).value("成本合计（元）").bold().setStyle()

            c++
            materialItems.forEach {
                r = row
                cell(
                    r++,
                    c
                ).value("${it.it}${if (it.it.unit.isBlank()) "" else "(${it.it.unit})"}")
                    .headerStyle().width(15.0)
                    .setStyle()
                cell(r++, c).value(it.it.price).format("0.00").setStyle()
                cell(r++, c).value(it.it.value).format("0.00").setStyle()
                cell(r++, c).value(it.value).format("0.00%").setStyle()
                cell(r++, c++).value(it.value * it.it.price * it.it.value).format("0.00").setStyle()
            }
            dictItems.forEach { (k, v) ->
                r = row
                cell(r++, c).value(k.dictName).headerStyle().width(15.0).setStyle()
                cell(r++, c).value(v.it.price).format("0.00").setStyle()
                cell(r++, c).value(v.it.value).format("0.00").setStyle()
                cell(r++, c).value(v.value).format("0.00%").setStyle()
                cell(r++, c++).value(v.value * v.it.price * v.it.value).format("0.00").setStyle()
            }
            var itemsSize = materialItems.size + dictItems.size
            if (itemsSize == 0) {
                r = row + 5
                itemsSize = 1
            }
            //税费
            cell(r, 1).value(taxFee)
            range(r, 1, r++, itemsSize).merge().format("0.00").setStyle()
            //制造费用
            cell(r, 1).value(totalFee)
            range(r, 1, r++, itemsSize).merge().bold().format("0.00")
                .setStyle()
            //原辅料成本
            cell(r, 1).value(recipe.materialCost)
            range(r, 1, r++, itemsSize).merge().bold().format("0.00")
                .setStyle()
            //原辅料成本/收率
            cell(r, 1).value((recipe.materialCost / recipe.requirement.yield))
            range(r, 1, r++, itemsSize).merge().bold().format("0.00")
                .setStyle()
            //包装费用
            cell(r, 1).value(recipe.packagingCost)
            range(r, 1, r++, itemsSize).merge().bold().format("0.00")
                .setStyle()
            //成本合计
            if (recipe.includeProductionCost) {
                cell(r, 1).value(recipe.cost + recipe.packagingCost)
            } else {
                cell(r, 1).value(totalFee + recipe.cost + recipe.packagingCost)
            }
            range(r, 1, r++, itemsSize).merge().bold().format("0.00")
                .setStyle()
        }
    }

    fun Excel.exportRecipe(recipe: Recipe): Int {
        val requirement = recipe.requirement
        val showRate = requirement.materialRelationConstraints.isNotEmpty()
        RecipeExt(recipe).apply {
            val titles =
                "项目${if (showRate) "\t最小耗液氨/硫酸系数\t最小耗液氨/硫酸量\t最大耗液氨/硫酸系数\t最大耗液氨/硫酸量" else ""}".split(
                    "\t"
                )
            val titles2 = "投料量(公斤)\t投料比\t费用合计\t原料单价".split("\t")
            val materials = recipe.materials.toSortedSet()
            var recipeIndicatorValues = requirement.indicatorRangeConstraints.values
            val itIndicatorValues =
                recipeIndicatorValues.filter { it.indicator.isRateToOther }.map { i ->
                    val itId = i.indicator.itId!!
                    val other = recipeIndicatorValues.find { i.indicator.otherId == it.id }!!.value

                    RecipeIndicatorValue<DoubleRange>(
                        itId,
                        DoubleRange(i.value.min * other.min / 100, i.value.max * other.max / 100)
                    ).apply {
                        indicator = requirement.indicators.find { it.id == id }
                            ?: throw IllegalArgumentException("未知指标:${id}")
                        scaledValue = indicator.scaleOf(value)
                    }
                }
            val rangeIndicators =
                (recipeIndicatorValues + itIndicatorValues).sortedBy { it.indicator.index }
            val columnSize = titles.size + titles2.size + rangeIndicators.size - 1

            var r = 0
            var c = 0
            //标题
            titles.forEach { s ->
                cell(r, c++).value(s).headerStyle().width(if (c in 1..5) 18.0 else 8.0).setStyle()
            }
            titles2.forEach { s ->
                cell(r, c++).value(s).headerStyle().width(8.0).setStyle()
            }

            rangeIndicators.map { it.indicator }.forEach { indicator ->
                cell(r, c++).value("${indicator.name}(${indicator.unit})").headerStyle().width(11.0)
                    .setStyle()
            }

            r++
            cell(r++, 0).value("配方目标最大值").bold().setStyle()
            cell(r++, 0).value("配方目标最小值").bold().setStyle()
            cell(r++, 0).value("实配值").bold().setStyle()
            c = titles.size + titles2.size - 1
            for (i in 1..3) {
                for (j in 1..c)
                    cell(i, j).value("/").setStyle()
            }
            c = titles.size
            // 投料量
            cell(3, c++).value(recipe.weight).bold().format("0.00").setStyle()
            // 投料比
            cell(3, c++).value(1.0).bold().format("0%").setStyle()
            // 费用合计
            cell(3, c++).value(recipe.materialCost).bold().format("0.00").setStyle()
            // 原料单价
            cell(3, c++).value("/").setStyle()

            rangeIndicators.forEach { rangeIndicator ->
                val indicator = rangeIndicator.indicator
                r = 1
                //配方目标最大值
                val max = rangeIndicator.scaledValue.max
                cell(r++, c).value(max).bold()
                    .format(if (IndicatorUnit.PERCENTAGE.eq(indicator.unit)) requirement.indicatorFormat else "")
                    .setStyle()

                //配方目标最小值
                val min = rangeIndicator.scaledValue.min
                cell(r++, c).value(min).bold()
                    .format(if (IndicatorUnit.PERCENTAGE.eq(indicator.unit)) requirement.indicatorFormat else "")
                    .setStyle()

                //实配值
                val value = when (indicator.type) {
                    RecipeIndicatorType.TOTAL_NUTRIENT -> ((materials.sumOf { it.totalNutrientWeight }) / requirement.targetWeight)
                    RecipeIndicatorType.PRODUCT_WATER -> ((materials.sumOf { it.waterWeight } - recipe.dryWaterWeight) / requirement.targetWeight)
                    RecipeIndicatorType.WATER -> (materials.sumOf { it.waterWeight } / materials.sumOf { it.weight })
                    RecipeIndicatorType.RATE_TO_OTHER -> {
                        val sumOf = materials.sumOf { it.indicatorWeight(indicator.otherId!!) }
                        if (sumOf == 0.0) {
                            0.0
                        } else
                            (materials.sumOf {
                                it.indicatorWeight(
                                    indicator.itId!!
                                )
                            } / sumOf)
                    }

                    else -> (materials.sumOf { it.indicatorWeight(indicator.id) } / requirement.targetWeight)
                }
                // 百分比最小误差值
                val minEpsilon = recipe.minEpsilon / 100
                val minDiff = (value - min).scale(recipe.scale + 2)
                val maxDiff = (value - max).scale(recipe.scale + 2)
                val valid =
                    (minDiff == 0.0 || minDiff > -minEpsilon) && (maxDiff == 0.0 || maxDiff < minEpsilon)
                cell(r++, c).value(value).bold()
                    .format(if (IndicatorUnit.PERCENTAGE.eq(indicator.unit)) requirement.indicatorFormat else "")
                    .fontColor(if (valid) "1fbb7d" else "FF0000").setStyle()
                c++
            }

            if (materials.isEmpty()) {
                return 0
            }
            //原料
            materials.forEach { material ->
                c = 0
                if (showRate && material.hasOverdose) {
                    cell(r + 1, c).wrapText().setStyle()
                }
                cell(r, c++).value("$material").wrapText().setStyle()
                val recipeRelation: RecipeRelation?
                val normal: DoubleRange?
                val relationValue: Pair<DoubleRange, DoubleRange>?
                val normalValue: DoubleRange?
                val relationName: String?
                if (showRate) {
                    recipeRelation = material.recipeRelationPair?.second
                    normal = recipeRelation?.normal
                    relationValue = material.relationValue
                    normalValue = relationValue?.first
                    relationName = material.relationName?.toNames(requirement)
                    // 最小耗液氨/硫酸系数
                    cell(r, c++).value(normal?.min)
                        .comment(if (normal?.min == null || relationName == null) null else "${material.name}最小耗${relationName}系数")
                        .format("0.000000000").setStyle()
                    // 最小耗液氨/硫酸量
                    cell(r, c++).value(normalValue?.min)
                        .comment(if (normalValue?.min == null || relationName == null) null else "${material.name}最小耗${relationName}数量")
                        .format("0.00").setStyle()
                    // 最大耗液氨/硫酸系数
                    cell(r, c++).value(normal?.max)
                        .comment(if (normal?.max == null || relationName == null) null else "${material.name}最大耗${relationName}系数")
                        .format("0.000000000").setStyle()
                    // 最大耗液氨/硫酸量
                    cell(r, c++).value(normalValue?.max)
                        .comment(if (normalValue?.max == null || relationName == null) null else "${material.name}最大耗${relationName}数量")
                        .format("0.00").setStyle()
                } else {
                    recipeRelation = null
                    relationValue = null
                    relationName = null
                }
                // 投料量
                if (showRate && material.hasOverdose) {
                    cell(r + 1, c).bold().format("0.00").setStyle()
                }
                cell(r, c++).value(material.weight).bold().format("0.00").setStyle()
                // 投料比
                if (showRate && material.hasOverdose) {
                    cell(r + 1, c).format("0.00%").setStyle()
                }
                cell(r, c++).value(material.weight / recipe.weight).format("0.00%").setStyle()
                // 费用合计
                if (showRate && material.hasOverdose) {
                    cell(r + 1, c).format("0.00").setStyle()
                }
                cell(r, c++).value(material.cost).format("0.00").setStyle()
                // 原料单价
                if (showRate && material.hasOverdose) {
                    cell(r + 1, c).format("0").setStyle()
                }
                cell(r, c++).value(material.price * 1000).format("0").setStyle()
                rangeIndicators.map { it.indicator }.forEach { indicator ->
                    val value: Double = when (indicator.type) {
                        RecipeIndicatorType.TOTAL_NUTRIENT -> material.totalNutrient
                        RecipeIndicatorType.PRODUCT_WATER -> material.indicators.waterValue
                        else -> material.indicators.valueOf(indicator.id)
                    }
                    if (showRate && material.hasOverdose) {
                        cell(r + 1, c)
                            .format(if (IndicatorUnit.PERCENTAGE.eq(indicator.unit)) requirement.indicatorFormat else "")
                            .setStyle()
                    }
                    cell(r, c++).value(value)
                        .format(if (IndicatorUnit.PERCENTAGE.eq(indicator.unit)) requirement.indicatorFormat else "")
                        .setStyle()
                }
                if (showRate && material.hasOverdose) {
                    c = 1
                    val r1 = r + 1
                    val overdose = recipeRelation?.overdose
                        ?: recipeRelation?.overdoseMaterial?.normal
                    val overdoseValue = relationValue?.second
                    // 最小耗液氨/硫酸系数
                    cell(r1, c++).value(overdose?.min)
                        .comment(if (overdose?.min == null || relationName == null) null else "${material.name}过量最小耗${relationName}系数")
                        .format("0.000000000").setStyle()
                    // 最小耗液氨/硫酸量
                    cell(r1, c++).value(overdoseValue?.min)
                        .comment(if (overdoseValue?.min == null || relationName == null) null else "${material.name}过量最小耗${relationName}数量")
                        .format("0.00").setStyle()
                    // 最大耗液氨/硫酸系数
                    cell(r1, c++).value(overdose?.max)
                        .comment(if (overdose?.max == null || relationName == null) null else "${material.name}过量最大耗${relationName}系数")
                        .format("0.000000000").setStyle()
                    // 最大耗液氨/硫酸量
                    cell(r1, c++).value(overdoseValue?.max)
                        .comment(if (overdoseValue?.max == null || relationName == null) null else "${material.name}过量最大耗${relationName}数量")
                        .format("0.00").setStyle()
                    for (i in 0..columnSize) {
                        if (i !in 1..4) {
                            range(r, i, r1, i).merge()
                        }
                    }
                    r++
                }
                r++
            }
            return r
        }
    }


}