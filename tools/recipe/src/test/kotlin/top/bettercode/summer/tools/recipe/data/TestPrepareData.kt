package top.bettercode.summer.tools.recipe.data

import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.Row
import org.springframework.core.io.ClassPathResource
import top.bettercode.summer.tools.excel.read.CellGetter
import top.bettercode.summer.tools.excel.read.ExcelReader
import top.bettercode.summer.tools.excel.read.RowGetter
import top.bettercode.summer.tools.optimal.Operator
import top.bettercode.summer.tools.recipe.RecipeRequirement
import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.criteria.RecipeCondition
import top.bettercode.summer.tools.recipe.criteria.RecipeRelation
import top.bettercode.summer.tools.recipe.criteria.TermThen
import top.bettercode.summer.tools.recipe.indicator.*
import top.bettercode.summer.tools.recipe.material.MaterialCondition
import top.bettercode.summer.tools.recipe.material.RecipeMaterial
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs.Companion.toMaterialIDs
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs.Companion.toRelationMaterialIDs
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs.Companion.toReplacebleMaterialIDs
import top.bettercode.summer.tools.recipe.material.id.RelationMaterialIDs
import top.bettercode.summer.tools.recipe.material.id.ReplacebleMaterialIDs
import top.bettercode.summer.tools.recipe.productioncost.*
import java.math.BigDecimal
import java.util.*
import java.util.stream.Collectors

/**
 * @author Peter Wu
 */
object TestPrepareData {

    const val INDICATOR_NAME_STRING =
        "总养分 氮 磷 钾 氯离子 产品水分 物料水分 水溶磷率 水溶磷 硝态氮 硼 锌 锰 铜 铁 钼 镁 硫 钙 有机质 腐植酸 黄腐酸 活性菌 硅 指标23 指标24 指标25 指标26 指标27 指标28 指标29 指标30 指标31 指标32 指标33 指标34 指标35 指标36 指标37 指标38 指标39 指标40 指标41 指标42 指标43 指标44 指标45 指标46 指标47 指标48 指标49 指标50"
    val indicatorNames: List<String> = INDICATOR_NAME_STRING.split(" +".toRegex())
    val indicators = indicatorNames.mapIndexed { i, name ->
        RecipeIndicator(
            index = i,
            id = name,
            name = name,
            unit = if ("活性菌" == name) IndicatorUnit.BILLION.unit else IndicatorUnit.PERCENTAGE.unit,
            type = when (name) {
                "总养分" -> RecipeIndicatorType.TOTAL_NUTRIENT
                "氮", "磷", "钾" -> RecipeIndicatorType.NUTRIENT
                "产品水分" -> RecipeIndicatorType.PRODUCT_WATER
                "物料水分" -> RecipeIndicatorType.WATER
                "水溶磷率" -> RecipeIndicatorType.RATE_TO_OTHER
                else -> RecipeIndicatorType.GENERAL
            },
            itId = if ("水溶磷率" == name) "水溶磷" else null,
            otherId = if ("水溶磷率" == name) "磷" else null
        )
    }


    fun readRequirement(productName: String): RecipeRequirement {
        // 不能混用的原料 不使用的原料 原料约束 限用原料 成份原料约束 项目  总养分 氮含量 磷含量 水溶磷率 钾含量 氯离子 产品水分 硼 锌 防结粉用量（公斤/吨产品）
        // 防结油用量（公斤/吨产肥 ） 喷浆专用尿素用量（公斤/吨） 磷酸耗液氨系数 硫酸耗液氨系数 再浆耗液氨系数 磷铵耗液氮系数
        val workbook =
            ReadableWorkbook(ClassPathResource("配方报价管理系统需求清单.xlsx").inputStream)
        val sheet = workbook
            .findSheet(productName)
            .orElseThrow { RuntimeException("找不到配方$productName") }
        val rows = sheet.openStream()
            .collect(Collectors.toMap({ obj: Row -> obj.rowNum }, { r: Row -> r }))
        // 工厂
        val factory = rows[1]!!.getCellAsString(4).orElseThrow { RuntimeException("找不到工厂") }

        // 附加条件起始行
        val conditionStartRow = 13
        // 附加条件起始列
        var conditionStartCol = 0
        // 特殊价格
        val specialPriceNameCol = 0
        conditionStartCol++
        val specialPriceCol = conditionStartCol
        conditionStartCol++
        val specialPrice: MutableMap<String, Double> = HashMap()
        rows.values
            .filter { row: Row -> row.rowNum > conditionStartRow }
            .forEach { row: Row ->
                val materialName = row.getCellAsString(specialPriceNameCol).orElse(null)
                if (!materialName.isNullOrBlank()) {
                    row.getCellAsNumber(specialPriceCol)
                        .ifPresent { price: BigDecimal ->
                            specialPrice[materialName] = price.toDouble()
                        }
                }
            }

        //读取工厂价格
        val materialPrices = readPrices(factory, specialPrice)
        val materials = readMaterials(materialPrices)
        val materialIds = materials.map { it.id }

        // 不能混用的原料
        val notMixMaterialCol = conditionStartCol
        conditionStartCol++
        val notMixMaterials = mutableListOf<Array<MaterialIDs>>()
        rows.values
            .filter { row: Row -> row.rowNum > conditionStartRow }
            .forEach { row: Row ->
                row.getCellAsString(notMixMaterialCol)
                    .ifPresent { str: String ->
                        if (str.isNotBlank()) {
                            val split = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                            val typedArray = split.map { s: String ->
                                materialIds.filter { it.contains(s) }.toMaterialIDs()
                            }.filter { it.ids.isNotEmpty() }

                            if (typedArray.size > 1) {
                                notMixMaterials.add(typedArray.toTypedArray())
                            }
                        }
                    }
            }

        // 不使用的原料
        val notUseMaterialCol = conditionStartCol
        conditionStartCol++
        val noUseMaterials = mutableSetOf<String>()
        rows.values
            .filter { row: Row -> row.rowNum > conditionStartRow }
            .forEach { row: Row ->
                row.getCellAsString(notUseMaterialCol)
                    .ifPresent { str: String? ->
                        if (!str.isNullOrBlank()) {
                            noUseMaterials.add(str)
                        }
                    }
            }

        // 原料约束
        val materialReqCol = conditionStartCol
        conditionStartCol++
        // 原料片段-仅用
        val materialIDConstraints = mutableListOf<TermThen<MaterialIDs, MaterialIDs>>()
        rows.values
            .filter { row: Row -> row.rowNum > conditionStartRow }
            .forEach { row: Row ->
                row.getCellAsString(materialReqCol)
                    .ifPresent { str: String ->
                        if (str.isNotBlank()) {
                            val split = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                            val key = split[0]
                            val array = materialIds.filter { it.contains(key) }.toMaterialIDs()
                            materialIDConstraints.add(
                                TermThen(
                                    array,
                                    Arrays.copyOfRange(split, 1, split.size).toMaterialIDs()
                                )
                            )
                        }
                    }
            }

        // 限用原料
        val limitUseMaterialCol = conditionStartCol
        conditionStartCol++
        val useMaterials = mutableSetOf<String>()
        rows.values
            .filter { row: Row -> row.rowNum > conditionStartRow }
            .forEach { row: Row ->
                row.getCellAsString(limitUseMaterialCol)
                    .ifPresent { str: String? ->
                        if (!str.isNullOrBlank()) {
                            useMaterials.add(str)
                        }
                    }
            }

        // 成份原料约束
        val materialIDIndicators = mutableListOf<RecipeIndicatorValue<MaterialIDs>>()
        rows.values
            .filter { row: Row -> row.rowNum > conditionStartRow }
            .forEach { row: Row ->
                row.getCellAsString(conditionStartCol)
                    .ifPresent { str: String ->
                        if (str.isNotBlank()) {
                            val split = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                            val key = split[0]
                            val indicator = RecipeIndicatorValue(
                                id = key,
                                value = Arrays.copyOfRange(split.toTypedArray(), 1, split.size)
                                    .toMaterialIDs()
                            )
                            materialIDIndicators.add(indicator)
                        }
                    }
            }

        // 条件约束
        conditionStartCol++
        val conditionCol = conditionStartCol
        val materialConditions = mutableListOf<TermThen<MaterialCondition, MaterialCondition>>()
        rows.values
            .filter { row: Row -> row.rowNum > conditionStartRow }
            .forEach { row: Row ->
                row.getCellAsString(conditionCol)
                    .ifPresent { str: String ->
                        if (str.isNotBlank()) {
                            val split = str.split(" +".toRegex()).dropLastWhile { it.isEmpty() }
                            if (split.size == 2) {
                                val condition1 = ofMaterialConstraint(materialIds, split[0])
                                val condition2 = ofMaterialConstraint(materialIds, split[1])
                                materialConditions.add(TermThen(condition1, condition2))
                            }
                        }
                    }
            }

        // 成品成份要求
        var index = 2
        val targetMaxLimitRow = rows[10]
        val targetMinLimitRow = rows[11]
        val rangeIndicators = mutableListOf<RecipeIndicatorValue<DoubleRange>>()
        // 0     1     2     3       4      5       6        7         8     9     10  11
        // 总养份 氮含量 磷含量 水溶磷率 钾含量  氯离子   产品水分   物料水分   硼    锌
        // 总养分 氮     磷    钾      氯离子  产品水分 物料水分   水溶磷率   水溶磷 硝态氮 硼   锌
        for (i in 0..9) {
            val min =
                ((targetMinLimitRow!!.getCell(index).value as BigDecimal).toDouble() * 100)
            val max =
                ((targetMaxLimitRow!!.getCell(index++).value as BigDecimal).toDouble() * 100)
            val indicator = when (i) {
                //总养分
                0 -> RecipeIndicatorValue(
                    id = indicatorNames[0],
                    value = DoubleRange(min, max),
                )

                //水溶磷率
                3 -> RecipeIndicatorValue(
                    id = indicatorNames[7],
                    value = DoubleRange(min, max),
                )
                //氮
                1 -> RecipeIndicatorValue(
                    id = indicatorNames[1],
                    value = DoubleRange(min, max),
                )
                //磷
                2 -> RecipeIndicatorValue(
                    id = indicatorNames[2],
                    value = DoubleRange(min, max),
                )
                //钾
                4 -> RecipeIndicatorValue(
                    id = indicatorNames[3],
                    value = DoubleRange(min, max),
                )

                //氯离子
                5 -> RecipeIndicatorValue(
                    id = indicatorNames[4],
                    value = DoubleRange(min, max),
                )

                //产品水分
                6 -> RecipeIndicatorValue(
                    id = indicatorNames[5],
                    value = DoubleRange(min, max),
                )

                //物料水分
                7 -> RecipeIndicatorValue(
                    id = indicatorNames[6],
                    value = DoubleRange(min, max),
                )

                //硼
                8 -> RecipeIndicatorValue(
                    id = indicatorNames[10],
                    value = DoubleRange(min, max),
                )

                //锌
                9 -> RecipeIndicatorValue(
                    id = indicatorNames[11],
                    value = DoubleRange(min, max),
                )

                else -> RecipeIndicatorValue(
                    id = indicatorNames[i],
                    value = DoubleRange(min, max),
                )
            }
            rangeIndicators.add(indicator)
        }

        val limitRowStart = 4
        val maxLimitCol = 7
        val minLimitCol = 8

        // 原料使用约束
        val materialRangeConstraints = mutableListOf<TermThen<MaterialIDs, DoubleRange>>()
        for (i in 0..2) {
            var materialNameFragment = rows[limitRowStart]!!.getCellAsString(index).orElse(null)
            if (!materialNameFragment.isNullOrBlank()) {
                materialNameFragment = materialNameFragment
                    .replace("用量（公斤/吨）", "")
                    .replace("用量（公斤/吨产肥 ）", "")
                    .replace("用量（公斤/吨产肥）", "")
                    .replace("用量（公斤/吨产品）", "")
                val maxUse = rows[maxLimitCol]!!
                    .getCellAsNumber(index)
                    .orElseThrow { RuntimeException("找不到用量") }
                val minUse = rows[minLimitCol]!!
                    .getCellAsNumber(index)
                    .orElseThrow { RuntimeException("找不到用量") }
                val array = materialIds.filter { it.contains(materialNameFragment) }.toMaterialIDs()
                materialRangeConstraints.add(
                    TermThen(
                        array,
                        DoubleRange(minUse.toDouble(), maxUse.toDouble())
                    )
                )
            }
            index++
        }
        // 原料之间的用量关系
        val materialRelationConstraints =
            mutableListOf<TermThen<ReplacebleMaterialIDs, MutableList<TermThen<RelationMaterialIDs, RecipeRelation>>>>()
        // 液氨
        for (i in 0..4) {
            var materialNameFragment = rows[limitRowStart]!!.getCellAsString(index).orElse(null)
            if (!materialNameFragment.isNullOrBlank()) {
                val hasRelation = materialNameFragment.contains("氯化钾")
                val isOverdose = materialNameFragment.contains("过量")
                if (isOverdose) {
                    materialNameFragment = materialNameFragment.replace("过量", "")
                }
                materialNameFragment = materialNameFragment
                    .replace("耗液氨系数", "")
                    .replace("耗液氮系数", "")
                    .replace("氯化钾反应需(.*)量".toRegex(), "$1")
                    .replace("氯化钾反应所需(.*)量".toRegex(), "$1")
                val maxUse = rows[maxLimitCol]!!.getCell(index).value as BigDecimal
                val minUse = rows[minLimitCol]!!.getCell(index).value as BigDecimal
                val m1 = materialIds.filter { isNeedLiquidAmmon(materialNameFragment, it) }
                val m2 = materialIds.filter { it == LIQUID_AMMONIA }
                val relationIds =
                    if (hasRelation) materialIds.filter { it.contains("氯化钾") } else null

                if (m1.isNotEmpty() && m2.isNotEmpty()) {
                    val replacebleMaterialIDs =
                        m2.toMaterialIDs().replace(LA_2_CAUSE_RATIO, AMMONIUM_CARBONATE)
                    var find = materialRelationConstraints.find { it.term == replacebleMaterialIDs }
                    if (find == null) {
                        find = TermThen(replacebleMaterialIDs, mutableListOf())
                        materialRelationConstraints.add(find)
                    }
                    val relationMaterialIDs = m1.toRelationMaterialIDs(relationIds?.toMaterialIDs())
                    var findRelation = find.then.find { it.term == relationMaterialIDs }
                    val doubleRange =
                        DoubleRange(minUse.toDouble(), maxUse.toDouble())
                    if (findRelation == null) {
                        findRelation = TermThen(relationMaterialIDs, RecipeRelation(doubleRange))
                        find.then.add(findRelation)
                    }
                    if (isOverdose) {
                        findRelation.then.overdoseMaterial = RecipeRelation(doubleRange)
                    } else {
                        findRelation.then.normal = doubleRange
                    }
                }
            }
            index++
        }
        // 硫酸
        for (i in 0..1) {
            var materialNameFragment = rows[limitRowStart]!!.getCellAsString(index).orElse(null)
            if (!materialNameFragment.isNullOrBlank()) {
                val isOverdose = materialNameFragment.contains("过量")
                if (isOverdose) {
                    materialNameFragment = materialNameFragment.replace("过量", "")
                }
                materialNameFragment = materialNameFragment.replace("反应所需硫酸系数", "")
                    .replace("反应需硫酸量系数", "")
                val maxUse = rows[maxLimitCol]!!.getCell(index).value as BigDecimal
                val minUse = rows[minLimitCol]!!.getCell(index).value as BigDecimal
                val m1 = materialIds.filter { it.contains(materialNameFragment) }
                val m2 = materialIds.filter { it == SULFURIC_ACID }

                if (m1.isNotEmpty() && m2.isNotEmpty()) {
                    val replacebleMaterialIDs = m2.toReplacebleMaterialIDs()
                    var find = materialRelationConstraints.find { it.term == replacebleMaterialIDs }
                    if (find == null) {
                        find = TermThen(replacebleMaterialIDs, mutableListOf())
                        materialRelationConstraints.add(find)
                    }
                    val doubleRange =
                        DoubleRange(minUse.toDouble(), maxUse.toDouble())
                    val relationMaterialIDs = m1.toRelationMaterialIDs()
                    var findRelation = find.then.find { it.term == relationMaterialIDs }
                    if (findRelation == null) {
                        findRelation = TermThen(relationMaterialIDs, RecipeRelation(doubleRange))
                        find.then.add(findRelation)
                    }
                    if (isOverdose) {
                        findRelation.then.overdose = doubleRange
                    } else {
                        findRelation.then.normal = doubleRange
                    }
                }
            }
            index++
        }

        // 能耗费用
        val materialItems: List<RecipeOtherMaterial> = listOf(
            RecipeOtherMaterial(
                index = 9001,
                id = "籽煤",
                name = "籽煤",
                price = 1650.0,
                value = 0.01
            ),
            RecipeOtherMaterial(
                index = 9002,
                id = "生物质",
                name = "生物质",
                price = 1000.0,
                value = 0.01
            ),
        )
        // 其他固定费用
        val dictItems = sortedMapOf(
            DictType.STAFF to Cost(1.0, 42.0),
            DictType.DEPRECIATION to Cost(1.0, 52.0),
            DictType.OTHER to Cost(1.0, 62.0),
        )
        // 费用增减
        val changes: List<CostChangeLogic> = listOf(
            CostChangeLogic(
                type = ChangeLogicType.WATER_OVER,
                materialId = listOf("液氨"),
                exceedValue = 50.0,
                eachValue = 1.0,
                changeItems = listOf(ChangeItem(ChangeItemType.MATERIAL, "籽煤")),
                changeValue = 0.01
            ),
            CostChangeLogic(
                type = ChangeLogicType.OVER,
                materialId = listOf("硫酸"),
                exceedValue = 100.0,
                eachValue = 1.0,
                changeItems = listOf(ChangeItem(ChangeItemType.DICT, "STAFF")),
                changeValue = 0.01
            ),
            CostChangeLogic(type = ChangeLogicType.OTHER, changeValue = 0.01)
        )

        val productionCost = ProductionCost(
            materialItems = materialItems,
            dictItems = dictItems,
            taxRate = 0.09,
            taxFloat = 15.0,
            changes = changes
        )

        val relationIndexList = listOf(
            ReplacebleMaterialIDs(SULFURIC_ACID),
            ReplacebleMaterialIDs(
                id = arrayOf(LIQUID_AMMONIA),
                LA_2_CAUSE_RATIO,
                MaterialIDs(AMMONIUM_CARBONATE)
            )
        )

        val requirement = RecipeRequirement(
            id = productName,
            targetWeight = 1000.0,
            indicators = indicators,
            materials = materials,
            productionCost = productionCost,
            packagingMaterials = listOf(
                RecipeOtherMaterial(
                    index = 0,
                    id = "高塔彩袋50公斤",
                    name = "高塔彩袋50公斤",
                    unit = "个",
                    price = 2.6,
                    value = 20.05
                )
            ),
            indicatorRangeConstraints = RecipeRangeIndicators(rangeIndicators),
            indicatorMaterialIDConstraints = RecipeMaterialIDIndicators(materialIDIndicators),
            keepMaterialConstraints = useMaterials.toMaterialIDs(),
            noUseMaterialConstraints = noUseMaterials.toMaterialIDs(),
            notMixMaterialConstraints = notMixMaterials,
            materialRangeConstraints = materialRangeConstraints,
            materialIDConstraints = materialIDConstraints,
            materialRelationConstraints = materialRelationConstraints.map {
                TermThen(
                    it.term,
                    it.then.toList()
                )
            }.sortedBy { relationIndexList.indexOf(it.term) },
            materialConditionConstraints = materialConditions,
            indicatorScale = 5
        )
        return requirement
    }

    private fun ofMaterialConstraint(
        materialsNames: List<String>,
        desc: String
    ): MaterialCondition {
        val split = desc.split(">=|<=|=|!=|>|<".toRegex()).dropLastWhile { it.isEmpty() }
        val materials = materialsNames.filter { it.contains(split[0]) }.toMaterialIDs()
        val value = split[1].toDouble()
        val typeStr = desc.substringAfter(split[0]).substringBeforeLast(split[1])
        val op = when (typeStr) {
            "=" -> Operator.EQ
            ">" -> Operator.GT
            "<" -> Operator.LT
            ">=" -> Operator.GE
            "<=" -> Operator.LE
            else -> throw RuntimeException("不支持的操作符")
        }
        return MaterialCondition(materials, RecipeCondition(op, value))
    }


    /** 获取原料成份,key: 原料名称 value: 原料成份  */
    private fun readMaterials(materialPrices: Map<String, Double?>): List<RecipeMaterial> {
        val rowGetter = RowGetter.of(
            CellGetter.of("大类", MaterialForm::category),
            CellGetter.of("原料名称", MaterialForm::name),
            CellGetter.of("原料形态", MaterialForm::form),
            CellGetter.of("氮含量", MaterialForm::nitrogen).defaultValue(0.0),
            CellGetter.of("磷含量", MaterialForm::phosphorus).defaultValue(0.0),
            CellGetter.of("钾含量", MaterialForm::potassium).defaultValue(0.0),
            CellGetter.of("氯离子", MaterialForm::chlorine).defaultValue(0.0),
            CellGetter.of("水分", MaterialForm::water).defaultValue(0.0),
            CellGetter.of("水溶磷率", MaterialForm::waterSolublePhosphorusRate)
                .defaultValue(0.0),
            CellGetter.of("水溶磷", MaterialForm::waterSolublePhosphorus)
                .defaultValue(0.0),
            CellGetter.of("硝态氮", MaterialForm::nitrateNitrogen).defaultValue(0.0),
            CellGetter.of("硼", MaterialForm::boron).defaultValue(0.0),
            CellGetter.of("锌", MaterialForm::zinc).defaultValue(0.0),
            CellGetter.of("锰", MaterialForm::manganese).defaultValue(0.0),
            CellGetter.of("铜", MaterialForm::copper).defaultValue(0.0),
            CellGetter.of("铁", MaterialForm::iron).defaultValue(0.0),
            CellGetter.of("钼", MaterialForm::molybdenum).defaultValue(0.0),
            CellGetter.of("镁", MaterialForm::magnesium).defaultValue(0.0),
            CellGetter.of("硫", MaterialForm::sulfur).defaultValue(0.0),
            CellGetter.of("钙", MaterialForm::calcium).defaultValue(0.0),
            CellGetter.of("有机质（%）", MaterialForm::organicMatter).defaultValue(0.0),
            CellGetter.of("腐植酸", MaterialForm::humicAcid).defaultValue(0.0),
            CellGetter.of("黄腐酸", MaterialForm::fulvicAcid).defaultValue(0.0),
            CellGetter.of("活性菌", MaterialForm::activeBacteria).defaultValue(0.0),
            CellGetter.of("硅", MaterialForm::silicon).defaultValue(0.0),
            CellGetter.of("指标23", MaterialForm::index23).defaultValue(0.0),
            CellGetter.of("指标24", MaterialForm::index24).defaultValue(0.0),
            CellGetter.of("指标25", MaterialForm::index25).defaultValue(0.0),
            CellGetter.of("指标26", MaterialForm::index26).defaultValue(0.0),
            CellGetter.of("指标27", MaterialForm::index27).defaultValue(0.0),
            CellGetter.of("指标28", MaterialForm::index28).defaultValue(0.0),
            CellGetter.of("指标29", MaterialForm::index29).defaultValue(0.0),
            CellGetter.of("指标30", MaterialForm::index30).defaultValue(0.0),
            CellGetter.of("指标31", MaterialForm::index31).defaultValue(0.0),
            CellGetter.of("指标32", MaterialForm::index32).defaultValue(0.0),
            CellGetter.of("指标33", MaterialForm::index33).defaultValue(0.0),
            CellGetter.of("指标34", MaterialForm::index34).defaultValue(0.0),
            CellGetter.of("指标35", MaterialForm::index35).defaultValue(0.0),
            CellGetter.of("指标36", MaterialForm::index36).defaultValue(0.0),
            CellGetter.of("指标37", MaterialForm::index37).defaultValue(0.0),
            CellGetter.of("指标38", MaterialForm::index38).defaultValue(0.0),
            CellGetter.of("指标39", MaterialForm::index39).defaultValue(0.0),
            CellGetter.of("指标40", MaterialForm::index40).defaultValue(0.0),
            CellGetter.of("指标41", MaterialForm::index41).defaultValue(0.0),
            CellGetter.of("指标42", MaterialForm::index42).defaultValue(0.0),
            CellGetter.of("指标43", MaterialForm::index43).defaultValue(0.0),
            CellGetter.of("指标44", MaterialForm::index44).defaultValue(0.0),
            CellGetter.of("指标45", MaterialForm::index45).defaultValue(0.0),
            CellGetter.of("指标46", MaterialForm::index46).defaultValue(0.0),
            CellGetter.of("指标47", MaterialForm::index47).defaultValue(0.0),
            CellGetter.of("指标48", MaterialForm::index48).defaultValue(0.0),
            CellGetter.of("指标49", MaterialForm::index49).defaultValue(0.0),
            CellGetter.of("指标50", MaterialForm::index50).defaultValue(0.0)
        )

        // 读取 Excel
        var materialForms =
            ExcelReader.of(ClassPathResource("配方报价管理系统需求清单.xlsx").inputStream).use {
                it
                    .sheet("原料成分表")
                    .column(1)
                    .getData<MaterialForm, MaterialForm>(rowGetter)
            }

        // 转换为Map
        materialForms = materialForms.filter { c: MaterialForm -> !c.name.isNullOrBlank() }
        val materials: MutableList<RecipeMaterial> = mutableListOf()
        var index = 0
        for (materialForm in materialForms) {
            val materialName = materialForm.name!!
            val materialPrice = materialPrices[materialName] ?: continue
            val indicators = mutableListOf<RecipeIndicatorValue<Double>>()
            var i = 0
            //跳过总养分
            // 氮 磷 钾
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.nitrogen!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.phosphorus!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.potassium!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.chlorine!! * 100),
                )
            )
            //跳过产品水份
            ++i
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.water!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.waterSolublePhosphorusRate!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.waterSolublePhosphorus!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.nitrateNitrogen!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.boron!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.zinc!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.manganese!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.copper!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.iron!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.molybdenum!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.magnesium!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.sulfur!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.calcium!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.organicMatter!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.humicAcid!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.fulvicAcid!! * 100),
                )
            )
            //活性菌
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = materialForm.activeBacteria!!,
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.silicon!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index23!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index24!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index25!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index26!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index27!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index28!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index29!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index30!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index31!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index32!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index33!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index34!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index35!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index36!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index37!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index38!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index39!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index40!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index41!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index42!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index43!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index44!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index45!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index46!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index47!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index48!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index49!! * 100),
                )
            )
            indicators.add(
                RecipeIndicatorValue(
                    id = indicatorNames[++i],
                    value = (materialForm.index50!! * 100),
                )
            )

            val material = RecipeMaterial(
                index = index,
                id = materialName,
                name = materialName,
                price = materialPrice / 1000,
                indicators = RecipeValueIndicators(indicators.filter { it.value > 0 })
            )
            materials.add(material)
            index++
        }
        return materials
    }

    /** 获取原料价格,key: 原料名称 value: 原料价格  */
    private fun readPrices(
        factory: String,
        specialPrices: Map<String, Double>
    ): Map<String, Double?> {
        // 读取原料价格:原料名称 荆州 宜城 应城 宁陵 平原 眉山 新疆 铁岭 肇东 佳木斯
        val rowGetter = RowGetter.of(
            CellGetter.of("原料名称", MaterialFactoryPriceForm::name),
            CellGetter.of(
                "报价日期",
                String::class.java
            ) { _: MaterialFactoryPriceForm, _: String? -> },
            CellGetter.of("荆州", MaterialFactoryPriceForm::jingzhou),
            CellGetter.of("宜城", MaterialFactoryPriceForm::yicheng),
            CellGetter.of("应城", MaterialFactoryPriceForm::yingcheng),
            CellGetter.of("宁陵", MaterialFactoryPriceForm::ningling),
            CellGetter.of("平原", MaterialFactoryPriceForm::pingyuan),
            CellGetter.of("眉山", MaterialFactoryPriceForm::meishan),
            CellGetter.of("新疆", MaterialFactoryPriceForm::xinjiang),
            CellGetter.of("铁岭", MaterialFactoryPriceForm::tieling),
            CellGetter.of("肇东", MaterialFactoryPriceForm::zhaodong),
            CellGetter.of("佳木斯", MaterialFactoryPriceForm::jiamusi)
        )

        // 读取 Excel
        val materialPriceForms =
            ExcelReader.of(ClassPathResource("配方报价管理系统需求清单.xlsx").inputStream)
                .sheet("原料价格表")
                .column(2)
                .getData<MaterialFactoryPriceForm, MaterialFactoryPriceForm>(rowGetter)

        // 转换为Map  获取原料价格 key: 原料名称 value: 原料价格
        val materialPriceMap: Map<String, Double?> = materialPriceForms.stream()
            .filter { p: MaterialFactoryPriceForm -> p.name.isNotBlank() }
            .collect(
                Collectors.toMap(
                    MaterialFactoryPriceForm::name
                ) { p: MaterialFactoryPriceForm ->
                    val materialPrice = MaterialPriceData()
                    materialPrice["荆州"] = p.jingzhou
                    materialPrice["宜城"] = p.yicheng
                    materialPrice["应城"] = p.yingcheng
                    materialPrice["宁陵"] = p.ningling
                    materialPrice["平原"] = p.pingyuan
                    materialPrice["眉山"] = p.meishan
                    materialPrice["新疆"] = p.xinjiang
                    materialPrice["铁岭"] = p.tieling
                    materialPrice["肇东"] = p.zhaodong
                    materialPrice["佳木斯"] = p.jiamusi
                    materialPrice
                }).mapValues { entry: Map.Entry<String, MaterialPriceData> ->
                specialPrices[entry.key] ?: entry.value.getPrice(factory)
            }
        return materialPriceMap
    }

    /** 碳铵原料名称  */
    const val AMMONIUM_CARBONATE = "碳铵"

    /** 液氨原料名称  */
    const val LIQUID_AMMONIA = "液氨"

    /** 硫酸原料名称  */
    const val SULFURIC_ACID = "硫酸"

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

            "磷铵" -> {
                materialName?.matches(Regex(".*磷.*铵.*")) == true
            }

            else -> {
                materialName!!.contains(materialNameFragment!!)
            }
        }
        return needLiquidAmmon
    }
}
