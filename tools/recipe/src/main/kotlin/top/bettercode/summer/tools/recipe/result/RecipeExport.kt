package top.bettercode.summer.tools.recipe.result

import top.bettercode.summer.tools.excel.FastExcel
import top.bettercode.summer.tools.optimal.solver.OptimalUtil
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale
import top.bettercode.summer.tools.recipe.RecipeRequirement
import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.criteria.RecipeRelation
import top.bettercode.summer.tools.recipe.indicator.RecipeIndicatorType

/**
 *
 * @author Peter Wu
 */
object RecipeExport {

    fun FastExcel.exportMaterial(requirement: RecipeRequirement) {
        val materials = requirement.materials.toSortedSet()
        val indicators = if (materials.isEmpty()) {
            return
        } else
            materials.first().indicators.values.sortedBy { it.index }
        //标题
        cell(0, 0).value("原料名称").headerStyle().setStyle()
        cell(0, 1).value("价格").headerStyle().setStyle()
        var i = 2
        for (indicator in indicators) {
            val column = i++
            cell(0, column).value(indicator.name).headerStyle().setStyle()
        }
        //原料
        var r = 0
        for (matrial in materials) {
            val matrialName = matrial.name
            var c = 0
            // 原料名称
            cell(++r, c++).value(matrialName).setStyle()
            // 成本 单价
            cell(r, c++).value(matrial.price * 1000).setStyle()
            // 原料成份
            matrial.indicators.values.sortedBy { it.index }.forEachIndexed { index, indicator ->
                val column = c + index
                cell(r, column).value(indicator.value.scale()).width(8.0).format("0.0%").setStyle()
            }
        }
    }

    fun FastExcel.exportRequirement(requirement: RecipeRequirement) {
        var c = 1
        var r = 0
        cell(r++, c).value("项目").headerStyle().width(20.0).setStyle()
        cell(r++, c).value("配方目标最大值").setStyle()
        cell(r, c).value("配方目标最小值").setStyle()
        //配方目标
        c++
        val rangeIndicators = requirement.indicatorRangeConstraints.values.sortedBy { it.index }
        rangeIndicators.forEach {
            r = 0
            cell(r++, c).value(it.name).headerStyle().width(10.0).setStyle()
            cell(r++, c).value(it.value.max).format("0.00%").setStyle()
            cell(r++, c++).value(it.value.min).format("0.00%").setStyle()
        }
        val columnSize = rangeIndicators.size + 1
        //推优原料限制
        c = 1
        cell(r, c++).value("推优原料限制").height(20.0).setStyle()
        //指定用原料
        val useMaterials = requirement.useMaterialConstraints
        //不能用原料
        val noUseMaterials = requirement.noUseMaterialConstraints
        cell(r, c).value("指定用原料：${useMaterials}；不能用原料：${noUseMaterials}")
        range(r, c, r++, columnSize).merge().horizontalAlignment("left").setStyle()
        //推优原料用量范围
        c = 1
        val limitMaterials = requirement.materialRangeConstraints
        cell(r, c++).value("推优原料用量范围").height(20.0 * limitMaterials.size).setStyle()
        val limitMaterialsStr = limitMaterials.entries.joinToString("\n") {
            "${it.key} 用量范围：${it.value.min} 至 ${it.value.max} 公斤；"
        }
        cell(r, c).value(limitMaterialsStr)
        range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText().setStyle()
        // 推优原料用量限制
        c = 1
        val materialConditionConstraints = requirement.materialConditionConstraints
        cell(r, c++).value("推优原料用量范围").height(20.0 * materialConditionConstraints.size).setStyle()
        val materialConditionConstraintsStr = materialConditionConstraints.joinToString("\n") {
            "如果 ${it.first.materials} ${it.first.condition}公斤时，${it.first.materials} ${it.first.condition}公斤；"
        }
        cell(r, c).value(materialConditionConstraintsStr)
        range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText().setStyle()

        // 硫酸/液氨/碳铵计算规则
        c = 1
        val relationConstraints = requirement.materialRelationConstraints
        cell(r, c++).value("硫酸/液氨/碳铵计算规则").height(40.0 * relationConstraints.values.sumOf { it.values.size }).setStyle()
        val relationConstraintsStr = relationConstraints.entries.joinToString("\n\n") {
            "启用${it.key.idStr}计算规则${if (it.key.replaceIds == null) "\n" else " ${it.key.idStr}/${it.key.replaceIds}用量换算系数：${it.key.replaceRate}\n"}${
                it.value.entries.joinToString("\n") { v ->
                    val normal = v.value.normal
                    val overdose = v.value.overdose
                    val overdoseMaterial = v.value.overdoseMaterial
                    val overdoseMaterialNormal = overdoseMaterial?.normal
                    val overdoseMaterialOverdose = overdoseMaterial?.overdose
                    "${v.key.relationIds?.idStr ?: ""}使用 ${v.key.idStr} 时，耗${it.key.idStr}系数：${normal.min}-${normal.max}${
                        if (overdose != null) {
                            "\n${v.key.relationIds?.idStr ?: ""}使用 ${v.key.idStr} 时，过量耗${it.key.idStr}系数：${overdose.min}-${overdose.max}"
                        } else {
                            ""
                        }
                    }${
                        if (overdoseMaterialNormal != null) {
                            "\n${v.key.relationIds?.idStr ?: ""}使用过量 ${v.key.idStr} 时，耗${it.key.idStr}系数：${overdoseMaterialNormal.min}-${overdoseMaterialNormal.max}"
                        } else {
                            ""
                        }
                    }${
                        if (overdoseMaterialOverdose != null) {
                            "\n${v.key.relationIds?.idStr ?: ""}使用过量 ${v.key.idStr} 时，过量耗${it.key.idStr}系数：${overdoseMaterialOverdose.min}-${overdoseMaterialOverdose.max}"
                        } else {
                            ""
                        }
                    }"
                }
            }"
        }
        cell(r, c).value(relationConstraintsStr)
        range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText().setStyle()
        //指标指定用原料
        val materialIDIndicators = requirement.indicatorMaterialIDConstraints
        if (materialIDIndicators.isNotEmpty()) {
            c = 1
            cell(r, c++).value("指标指定用原料").height(20.0 * materialIDIndicators.size).setStyle()
            val materialIDIndicatorsStr = materialIDIndicators.entries.joinToString("\n") { "指标${it.value.name} 限用原料：${it.value.value}" }
            cell(r, c).value(materialIDIndicatorsStr)
            range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText().setStyle()
        }
        // 不能混用的原料
        val notMixMaterialConstraints = requirement.notMixMaterialConstraints
        if (notMixMaterialConstraints.isNotEmpty()) {
            c = 1
            cell(r, c++).value("不能混用的原料").height(20.0 * notMixMaterialConstraints.size).setStyle()
            val notMixMaterialConstraintsStr = notMixMaterialConstraints.joinToString("\n") {
                it.joinToString("和") + "不能混用"
            }
            cell(r, c).value(notMixMaterialConstraintsStr)
            range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText().setStyle()
        }
        // 指定原料约束
        val materialIDConstraints = requirement.materialIDConstraints
        if (materialIDConstraints.isNotEmpty()) {
            c = 1
            cell(r, c++).value("指定原料约束").height(20.0 * materialIDConstraints.size).setStyle()
            val materialIDConstraintsStr = materialIDConstraints.entries.joinToString("\n") {
                "${it.key}中指定使用原料：${it.value}"
            }
            cell(r, c).value(materialIDConstraintsStr)
            range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText().setStyle()
        }

        //其它产线信息
        c = 1
        cell(r, c++).value("其它产线信息").height(20.0).setStyle()
        cell(r, c).value("可用原料种类最大数：${requirement.maxUseMaterialNum}；单吨产品水份最大烘干量：${requirement.maxBakeWeight}公斤；")
        range(r, c, r++, columnSize).merge().horizontalAlignment("left").wrapText().setStyle()

    }


    fun FastExcel.exportRecipe(recipe: Recipe, showRate: Boolean = false) {
        val requirement = recipe.requirement
        RecipeExt(recipe).apply {
            val titles = "项目${if (showRate) "\t最小耗液氨/硫酸系数\t最小耗液氨/硫酸量\t最大耗液氨/硫酸系数\t最大耗液氨/硫酸量" else ""}\t投料量".split("\t")
            val materials = recipe.materials.toSortedSet()
            val rangeIndicators = requirement.indicatorRangeConstraints.values.sortedBy { it.index }
            val columnSize = titles.size + rangeIndicators.size

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
                for (j in 1..5)
                    cell(i, j).value("/").setStyle()
            }
            for (j in 1..4)
                cell(3, j).value("/").setStyle()
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
            // 费用合计
            cell(0, c).value("费用合计").headerStyle().width(8.0).setStyle()
            for (i in 1..2) {
                cell(i, c).value("/").setStyle()
            }
            cell(3, c).value(recipe.cost).bold().format("0.00").setStyle()
            //原料
            materials.forEach { material ->
                c = 0
                cell(r, c++).value(material.name).wrapText().setStyle()
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
                    recipeRelation = null
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
                // 费用合计
                cell(r, c++).value(material.cost).format("0.00").setStyle()
                if (showRate && material.hasOverdose) {
                    c = 1
                    val r1 = r + 1
                    val overdose = recipeRelation?.overdose
                            ?: recipeRelation?.overdoseMaterial?.normal
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