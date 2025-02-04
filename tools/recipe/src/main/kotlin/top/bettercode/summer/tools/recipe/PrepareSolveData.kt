package top.bettercode.summer.tools.recipe

import org.slf4j.LoggerFactory
import org.springframework.util.Assert
import top.bettercode.summer.tools.optimal.Expr
import top.bettercode.summer.tools.optimal.IVar
import top.bettercode.summer.tools.optimal.Operator
import top.bettercode.summer.tools.optimal.OptimalUtil.inTolerance
import top.bettercode.summer.tools.optimal.OptimalUtil.scale
import top.bettercode.summer.tools.optimal.Solver
import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.criteria.TermThen
import top.bettercode.summer.tools.recipe.criteria.UsageVar
import top.bettercode.summer.tools.recipe.indicator.IndicatorUnit
import top.bettercode.summer.tools.recipe.material.RecipeMaterialVar
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs.Companion.toMaterialIDs
import top.bettercode.summer.tools.recipe.productioncost.*
import top.bettercode.summer.tools.recipe.result.Recipe
import kotlin.math.abs
import kotlin.math.log10

/**
 *
 * @author Peter Wu
 */
data class PrepareSolveData(
    val defaultRecipeName: String,
    val epsilon: Double,
    val requirement: RecipeRequirement,
    val includeProductionCost: Boolean,
    val recipeMaterials: Map<String, RecipeMaterialVar>,
    val objectiveVars: List<IVar>,
    val materialItems: List<CarrierValue<RecipeOtherMaterial, IVar>>?,
    val dictItems: Map<DictType, CarrierValue<Cost, IVar>>?,
) {

    private val log = LoggerFactory.getLogger(PrepareSolveData::class.java)

    companion object {
        fun of(
            solver: Solver,
            requirement: RecipeRequirement,
            includeProductionCost: Boolean = true,
        ): PrepareSolveData {
            solver.apply {
                setTimeLimit(requirement.timeout)
                // 原料数量
                val materials = requirement.useMaterials
                val numRawMaterials = materials.size
                val numMaxMaterials = requirement.maxUseMaterialNum
                val targetWeight = requirement.targetWeight

                val recipeMaterials = materials.associateBy { it.id }.mapValues {
                    RecipeMaterialVar(it.value, numVar(0.0, targetWeight))
                }

                val materialVars = recipeMaterials.values.map { it.weight }

                // 进料口数量
                if (numMaxMaterials != null && numMaxMaterials in 1 until numRawMaterials) {
                    recipeMaterials.values.filter { !it.material.feedPortShare }.map { it.weight }
                        .atMost(numMaxMaterials)
                }

                // 不能混用的原料
                val notMixMaterials = requirement.notMixMaterialConstraints

                // 不选取不能同时使用的原料对
                notMixMaterials.forEach { notMixedMaterial ->
                    //一组变量至多有一个变量可取非零值
                    val noMixedVars = notMixedMaterial
                        .map { materialIDs: MaterialIDs ->
                            val vars = materialIDs.mapNotNull { recipeMaterials[it]?.weight }
                            if (vars.isEmpty()) return@map null
                            vars.sum()
                        }.filterNotNull()
                    noMixedVars.atMostOne()
                }


                // 定义产品净重 >=1000kg，含水，最大烘干量
                materialVars.between(
                    targetWeight,
                    if (requirement.maxBakeWeight == null)
                        Double.POSITIVE_INFINITY
                    else
                        (targetWeight + requirement.maxBakeWeight)
                )

                val rangeIndicators = requirement.indicatorRangeConstraints
                // 产品水分指标
                val waterRange = rangeIndicators.productWaterValue
                // 定义产品干净重
                val minDryWeight = targetWeight * (1 - (waterRange?.max ?: 0.0))
                val maxDryWeight = targetWeight * (1 - (waterRange?.min ?: 0.0))
                recipeMaterials.map {
                    val material = it.value
                    it.value.weight * (1 - material.indicators.waterValue)
                }.between(minDryWeight, maxDryWeight)

                // 添加成份约束条件
                // 成份要求 总养分 氮含量 磷含量 水溶磷率 钾含量 氯离子 产品水分 物料水分 硼 锌
                for (rangeIndicator in rangeIndicators) {
                    val range = rangeIndicator.scaledValue
                    val indicator = rangeIndicator.indicator
                    when {
                        indicator.isProductWater -> {
                            continue
                        }

                        indicator.isTotalNutrient -> {
                            recipeMaterials.map {
                                it.value.weight * it.value.totalNutrient
                            }.between(targetWeight * range.min, targetWeight * range.max)
                        }

                        indicator.isWater -> {
                            val sum = materialVars.sum()
                            recipeMaterials.map {
                                val material = it.value
                                val coeff = material.indicators.valueOf(indicator.id)
                                material.weight * coeff
                            }.between(sum * range.min, sum * range.max)
                        }

                        indicator.isRateToOther -> {
                            val otherVar = recipeMaterials.map {
                                val material = it.value
                                val coeff = material.indicators.valueOf(indicator.otherId!!)
                                material.weight * coeff
                            }.sum()

                            val itVar = recipeMaterials.map {
                                val material = it.value
                                val coeff =
                                    material.indicators.valueOf(indicator.id) * material.indicators.valueOf(
                                        indicator.otherId!!
                                    )
                                material.weight * coeff
                            }.sum()

                            itVar.ratioInRange(otherVar, range.min, range.max)
                        }

                        //目标包含多少亿
                        IndicatorUnit.BILLION.eq(indicator.unit) -> {
                            recipeMaterials.map {
                                val material = it.value
                                //每公斤包含多少亿
                                val coeff = material.indicators.valueOf(indicator.id)
                                material.weight * coeff
                            }.between(range.min, range.max)
                        }

                        else -> {
                            recipeMaterials.map {
                                val material = it.value
                                val coeff = material.indicators.valueOf(indicator.id)
                                material.weight * coeff
                            }.between(targetWeight * range.min, targetWeight * range.max)
                        }
                    }
                }


                // 原料用量
                val materialRangeConstraints = requirement.materialRangeConstraints
                materialRangeConstraints.forEach { (t, u) ->
                    val iVars = t.mapNotNull { recipeMaterials[it]?.weight }
                    if (Operator.GE == u.minOperator && Operator.LE == u.maxOperator) {
                        iVars.between(u.min, u.max)
                    } else {
                        when (u.minOperator) {
                            Operator.GE -> iVars.ge(u.min)
                            Operator.GT -> iVars.gt(u.min)
                            else -> throw IllegalArgumentException("原料用量范围配置错误${u.minOperator} ${u.min}")
                        }
                        when (u.maxOperator) {
                            Operator.LE -> iVars.le(u.max)
                            Operator.LT -> iVars.lt(u.max)
                            else -> throw IllegalArgumentException("原料用量范围配置错误${u.maxOperator} ${u.max}")
                        }
                    }
                }

                // 关联原料比率约束
                val materialRelationConstraints = requirement.materialRelationConstraints
                val calIds = mutableSetOf<String>()
                materialRelationConstraints.filter { it.then.isNotEmpty() }
                    .forEach { (ids, relation) ->
                        val useReplace = boolVar()
                        val consumeMaterialVars = ids.mapNotNull { recipeMaterials[it] }
                        consumeMaterialVars.forEach {
                            it.weight.leIf(0.0, useReplace)
                        }
                        val replaceConsumeMaterialVars =
                            ids.replaceIds?.mapNotNull { recipeMaterials[it] }
                        replaceConsumeMaterialVars?.forEach {
                            it.weight.leIfNot(0.0, useReplace)
                        }
                        val replaceRate = ids.replaceRate

                        relation.forEach { (t, u) ->
                            val normal = u.normal
                            val overdose = u.overdose
                            val overdoseMaterial = u.overdoseMaterial
                            val relationIds = t.relationIds

                            t.mapNotNull { recipeMaterials[it] }.forEach { m ->
                                val normalVars = mutableListOf<IVar>()
                                val overdoseVars = mutableListOf<IVar>()
                                //关联原料
                                val normalMinVars = mutableListOf<IVar>()
                                val normalMaxVars = mutableListOf<IVar>()
                                val overdoseMinVars = mutableListOf<IVar>()
                                val overdoseMaxVars = mutableListOf<IVar>()

                                //原料消耗变量初始化
                                consumeMaterialVars.forEach {
                                    val normalVar = numVar(0.0, targetWeight)
                                    normalVars.add(normalVar)
                                    val overdoseVar = numVar(0.0, targetWeight)
                                    overdoseVars.add(overdoseVar)
                                    it.consumes[m.id] =
                                        UsageVar(normal = normalVar, overdose = overdoseVar)
                                }
                                //替换原料消耗变量初始化
                                if (replaceRate != null)
                                    replaceConsumeMaterialVars?.forEach {
                                        val normalVar = numVar(0.0, targetWeight)
                                        normalVars.add(normalVar / replaceRate)
                                        val overdoseVar = numVar(0.0, targetWeight)
                                        overdoseVars.add(overdoseVar / replaceRate)
                                        it.consumes[m.id] =
                                            UsageVar(normal = normalVar, overdose = overdoseVar)
                                    }

                                //m 对应原料的用量变量
                                val normalWeight =
                                    if (relationIds == null) {//当无其他消耗m原料时，取本身用量
                                        m.weight
                                    } else {//当有其他消耗m原料时，如：氯化钾反应所需硫酸量耗液氨,取关联原料消耗汇总
                                        Assert.isTrue(
                                            calIds.contains(m.id),
                                            "有其他关联原料消耗此原料时，先计算每个关联原料消耗的此原料"
                                        )
                                        m.consumes.filterKeys { relationIds.contains(it) }.values.map { it.normal }
                                            .sum()
                                    }
                                //过量消耗原料用量变量
                                val overdoseWeight =
                                    if (relationIds == null) {//当无其他消耗m原料时，不存在过量消耗
                                        null
                                    } else {//当有其他消耗m原料时，如：氯化钾反应需过量硫酸耗液氨,取关联原料消耗汇总
                                        Assert.isTrue(
                                            calIds.contains(m.id),
                                            "有其他关联原料消耗此原料时，先计算每个关联原料消耗的此原料"
                                        )
                                        m.consumes.filterKeys { relationIds.contains(it) }.values.map { it.overdose }
                                            .sum()
                                    }

                                //原料消耗
                                if (normal != null) {
                                    normalMinVars.add(normalWeight * normal.min)
                                    normalMaxVars.add(normalWeight * normal.max)
                                }
                                if (overdose != null) {
                                    overdoseMinVars.add(normalWeight * overdose.min)
                                    overdoseMaxVars.add(normalWeight * overdose.max)
                                }

                                // 过量原料
                                if (overdoseMaterial != null && overdoseWeight != null) {
                                    val overdoseMaterialNormal = overdoseMaterial.normal
                                    val overdoseMaterialOverdose = overdoseMaterial.overdose
                                    //过量原料消耗
                                    if (overdoseMaterialNormal != null) {
                                        normalMinVars.add(overdoseWeight * overdoseMaterialNormal.min)
                                        normalMaxVars.add(overdoseWeight * overdoseMaterialNormal.max)
                                    }
                                    //过量原料过量消耗
                                    if (overdoseMaterialOverdose != null) {
                                        overdoseMinVars.add(overdoseWeight * overdoseMaterialOverdose.min)
                                        overdoseMaxVars.add(overdoseWeight * overdoseMaterialOverdose.max)
                                    }
                                }

                                normalVars.between(normalMinVars.sum(), normalMaxVars.sum())
                                overdoseVars.between(overdoseMinVars.sum(), overdoseMaxVars.sum())
                            }
                        }
                        consumeMaterialVars.forEach {
                            it.consumes.values.flatMap { c -> listOf(c.normal, c.overdose) }.sum()
                                .eq(it.weight)
                        }
                        replaceConsumeMaterialVars?.forEach {
                            it.consumes.values.flatMap { c -> listOf(c.normal, c.overdose) }.sum()
                                .eq(it.weight)
                        }
                        calIds.addAll(ids.ids)
                        ids.replaceIds?.let {
                            calIds.addAll(it)
                        }
                    }

                // 条件约束
                requirement.materialConditionConstraints.forEach { (whenCondition, thenCondition) ->
                    val whenVar =
                        whenCondition.materials.mapNotNull { recipeMaterials[it]?.weight }.sum()
                    val thenVar =
                        thenCondition.materials.mapNotNull { recipeMaterials[it]?.weight }.sum()
                    val whenCon = whenCondition.condition
                    val thenCon = thenCondition.condition
                    thenVar.expr(thenCon.operator, thenCon.value)
                        .onlyEnforceIf(whenVar.expr(whenCon.operator, whenCon.value))
                }
                //制造费用
                val materialItems: List<CarrierValue<RecipeOtherMaterial, IVar>>?
                val dictItems: Map<DictType, CarrierValue<Cost, IVar>>?
                val objectiveVars = recipeMaterials.values.map {
                    it.weight * it.price
                }.toMutableList()

                if (includeProductionCost) {
                    val productionCost = requirement.productionCost
                    val toZeros = productionCost.changes.filter { it.toZero }
                    val mIds = toZeros.flatMap {
                        it.changeItems!!.filter { it.type == ChangeItemType.MATERIAL }.map { it.id }
                    }
                    val dTypes = toZeros.flatMap {
                        it.changeItems!!.filter { it.type == ChangeItemType.DICT }
                            .map { DictType.valueOf(it.id) }
                    }

                    materialItems =
                        productionCost.materialItems
                            .map {
                                CarrierValue(
                                    it,
                                    if (mIds.contains(it.id)) numVar(0.0, 0.0) else numVar(1.0, 1.0)
                                )
                            }
                    dictItems =
                        productionCost.dictItems.mapValues {
                            CarrierValue(
                                it.value,
                                if (dTypes.contains(it.key)) numVar(0.0, 0.0) else numVar(1.0, 1.0)
                            )
                        }
                    //费用增减
                    var allChange = 1.0
                    productionCost.changes.filter { !it.toZero }.forEach { changeLogic ->
                        when (changeLogic.type) {
                            ChangeLogicType.WATER_OVER -> {
                                changeProductionCost(
                                    productionCost.changeWhenMaterialUsed,
                                    recipeMaterials,
                                    changeLogic,
                                    recipeMaterials.map {
                                        it.value.weight * it.value.indicators.waterValue
                                    }.sum(),
                                    materialItems,
                                    dictItems
                                )
                            }

                            ChangeLogicType.OVER -> {
                                changeProductionCost(
                                    productionCost.changeWhenMaterialUsed,
                                    recipeMaterials,
                                    changeLogic,
                                    null,
                                    materialItems,
                                    dictItems
                                )
                            }

                            ChangeLogicType.OTHER -> allChange += changeLogic.changeValue
                        }
                    }
                    //能耗费用
                    val energyFee = materialItems.map {
                        val value = it.value
                        //仅支持value>0,不支持当value<0时，value取0
                        value.ge(0.0)
                        value * it.it.cost
                    }.sum()
                    //人工+折旧费+其他费用
                    val otherFee = dictItems.values.map {
                        val value = it.value
                        //仅支持value>0,不支持当value<0时，value取0
                        value.ge(0.0)
                        value * it.it.cost
                    }.sum()
                    //税费 =（人工+折旧费+其他费用）*0.09+15
                    val taxFee = otherFee * productionCost.taxRate + productionCost.taxFloat
                    if (allChange < 0) {
                        allChange = 0.0
                    }
                    val fee = arrayOf(energyFee, otherFee, taxFee).sum() * allChange

                    objectiveVars.add(fee)
                } else {
                    materialItems = null
                    dictItems = null
                }

                return PrepareSolveData(
                    defaultRecipeName = this.name,
                    epsilon = epsilon,
                    requirement = requirement,
                    includeProductionCost = includeProductionCost,
                    recipeMaterials = recipeMaterials,
                    objectiveVars = objectiveVars,
                    materialItems = materialItems,
                    dictItems = dictItems
                )
            }
        }

        private fun Solver.changeProductionCost(
            changeWhenMaterialUsed: Boolean,
            materials: Map<String, RecipeMaterialVar>,
            changeLogic: CostChangeLogic,
            value: IVar?,
            materialItems: List<CarrierValue<RecipeOtherMaterial, IVar>>,
            dictItems: Map<DictType, CarrierValue<Cost, IVar>>,
        ) {
            val iVars = materials.filter { changeLogic.materialId?.contains(it.key) == true }
                .map { it.value.weight }
            if (iVars.isNotEmpty()) {
                val useMaterial = iVars.sum()
                val useVar = value ?: useMaterial

                val noUseThens = mutableListOf<Expr>()
                val exceedValue = changeLogic.exceedValue!!
                val changeRate = changeLogic.changeValue / changeLogic.eachValue!!
                changeLogic.changeItems!!.forEach { item ->
                    when (item.type) {
                        ChangeItemType.MATERIAL -> {//能耗费用
                            //(1+(value-exceedValue)/eachValue*changeValue)
                            val material = materialItems.find { it.it.id == item.id }
                            if (material != null) {
                                //change
                                val changeVar = (useVar - exceedValue) * changeRate
                                material.value += changeVar
                                if (changeWhenMaterialUsed) {
                                    noUseThens.add(changeVar.eqExpr(0.0))
                                }
                            }
                        }

                        ChangeItemType.DICT -> {
                            when (val dictType = DictType.valueOf(item.id)) {
                                DictType.ENERGY -> {
                                    materialItems.forEach {
                                        //change
                                        val changeVar = (useVar - exceedValue) * changeRate
                                        it.value += changeVar
                                        if (changeWhenMaterialUsed) {
                                            noUseThens.add(changeVar.eqExpr(0.0))
                                        }
                                    }
                                }

                                else -> {
                                    val cost = dictItems[dictType]
                                    if (cost != null) {
                                        //change
                                        val changeVar = (useVar - exceedValue) * changeRate
                                        cost.value += changeVar
                                        if (changeWhenMaterialUsed) {
                                            noUseThens.add(changeVar.eqExpr(0.0))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //仅支持useMaterial>0,不支持当useMaterial=0时，费用系数<0时，取费用系数=0
                //当使用原料为0时，changeVar=0.0
                if (changeWhenMaterialUsed) {
                    noUseThens.onlyEnforceIf(useMaterial.leExpr(0.0))
                }
            }
        }
    }

    @JvmOverloads
    fun solve(
        solver: Solver,
        minMaterialNum: Boolean,
        autoFixProductionCost: Boolean,
        recipeName: String? = null,
        minEpsilon: Double,
    ): Recipe? {
        solver.apply {
            val minimize = objectiveVars.minimize()
            var recipe: Recipe
            solve()
            if (!isOptimal()) {
                log.warn("${requirement.id}:${solver.name} ${solver.epsilon} Could not find optimal solution:${getResultStatus()}")
                return null
            } else {
                if (log.isDebugEnabled) {
                    log.debug("${requirement.id}:${solver.name} ${solver.epsilon} find optimal solution:${getResultStatus()}")
                }
                recipe = recipe(recipeName, minEpsilon, minimize.value)
            }
            //制造费用增减逻辑自动调整
            if (includeProductionCost && autoFixProductionCost) {
                val productionCost = requirement.productionCost
                val changes = productionCost.changes
                val recipeProductionCost = recipe.productionCost

                //当原料用量减少时，制造费用增加，自动处理相关原料用量为epsilon
                val excludeMid = changes.filter { it.type != ChangeLogicType.OTHER }.filter { c ->
                    val weight =
                        recipe.materials.filter { m -> c.materialId?.contains(m.id) == true }
                            .sumOf { it.weight }
                    val changeRate =
                        (weight - c.exceedValue!!) * c.changeValue / c.eachValue!!

                    (weight - epsilon).inTolerance(minEpsilon)
                            && changeRate > 0
                            && c.changeItems?.all { ci ->//仅受这组原料影响
                        when (ci.type) {
                            ChangeItemType.MATERIAL -> {
                                val mv =
                                    recipeProductionCost.materialItems.find { it.it.id == ci.id }?.value
                                mv != null && (mv - (1.0 + changeRate)).inTolerance(0.0)
                            }

                            ChangeItemType.DICT -> {
                                val dv =
                                    recipeProductionCost.dictItems[DictType.valueOf(ci.id)]?.value
                                dv != null && (dv - (1.0 + changeRate)).inTolerance(0.0)
                            }
                        }
                    } == true
                }.mapNotNull { it.materialId }.flatMap { it }

                //自动处理制造费用为0时，原料可以超过exceedValue限制
                val materialRangeConstraints = mutableListOf<TermThen<MaterialIDs, DoubleRange>>()
                val newChanges = changes.map { c ->
                    if (c.type == ChangeLogicType.OTHER) {
                        c
                    } else {
                        val weight =
                            recipe.materials.filter { m -> c.materialId?.contains(m.id) == true }
                                .sumOf { it.weight }
                        val changeRate =
                            (weight - c.exceedValue!!) * c.changeValue / c.eachValue!!
                        if (c.changeItems?.all { ci ->//仅受这组原料影响
                                when (ci.type) {
                                    ChangeItemType.MATERIAL -> {
                                        val mv =
                                            recipeProductionCost.materialItems.find { it.it.id == ci.id }?.value
                                        mv != null && mv.inTolerance(0.0) && (mv - (1.0 + changeRate)).inTolerance(
                                            0.0
                                        )
                                    }

                                    ChangeItemType.DICT -> {
                                        val dv =
                                            recipeProductionCost.dictItems[DictType.valueOf(ci.id)]?.value
                                        dv != null && dv.inTolerance(0.0) && (dv - (1.0 + changeRate)).inTolerance(
                                            0.0
                                        )
                                    }
                                }
                            } == true
                        ) {
                            materialRangeConstraints.add(
                                TermThen(
                                    c.materialId!!.toMaterialIDs(),
                                    if (changeRate > 0.0) DoubleRange(
                                        weight,
                                        requirement.targetWeight
                                    ) else DoubleRange(
                                        minOperator = Operator.GT,
                                        min = 0.0,
                                        maxOperator = Operator.LE,
                                        max = weight
                                    )
                                )
                            )
                            CostChangeLogic(
                                type = c.type,
                                materialId = c.materialId,
                                changeValue = changeRate,
                                eachValue = c.eachValue,
                                exceedValue = c.exceedValue,
                                changeItems = c.changeItems,
                                toZero = true
                            )
                        } else {
                            c
                        }
                    }
                }

                val excludeM = excludeMid.isNotEmpty()
                val productFix = materialRangeConstraints.isNotEmpty()
                if (excludeM || productFix) {
                    solver.reset()
                    return of(
                        solver = solver,
                        requirement = RecipeRequirement(
                            id = requirement.id,
                            productName = requirement.productName,
                            targetWeight = requirement.targetWeight,
                            yield = requirement.yield,
                            maxUseMaterialNum = requirement.maxUseMaterialNum,
                            maxBakeWeight = requirement.maxBakeWeight,
                            productionCost = if (productFix) ProductionCost(
                                materialItems = productionCost.materialItems,
                                dictItems = productionCost.dictItems,
                                taxRate = productionCost.taxRate,
                                taxFloat = productionCost.taxFloat,
                                changes = newChanges,
                                changeWhenMaterialUsed = productionCost.changeWhenMaterialUsed,
                            ) else productionCost,
                            indicators = requirement.indicators,
                            packagingMaterials = requirement.packagingMaterials,
                            materials = requirement.materials,
                            keepMaterialConstraints = requirement.keepMaterialConstraints,
                            noUseMaterialConstraints = if (excludeM) MaterialIDs(requirement.noUseMaterialConstraints + excludeMid) else requirement.noUseMaterialConstraints,
                            indicatorRangeConstraints = requirement.indicatorRangeConstraints,
                            materialRangeConstraints = if (productFix) requirement.materialRangeConstraints + materialRangeConstraints else requirement.materialRangeConstraints,
                            materialConditionConstraints = requirement.materialConditionConstraints,
                            materialRelationConstraints = requirement.materialRelationConstraints,
                            materialIDConstraints = requirement.materialIDConstraints,
                            indicatorMaterialIDConstraints = requirement.indicatorMaterialIDConstraints,
                            notMixMaterialConstraints = requirement.notMixMaterialConstraints,
                            indicatorScale = requirement.indicatorScale,
                            timeout = requirement.timeout
                        ),
                        includeProductionCost = true
                    ).solve(
                        solver = solver,
                        minMaterialNum = minMaterialNum,
                        recipeName = recipeName,
                        minEpsilon = minEpsilon,
                        autoFixProductionCost = true
                    )
                }
            }

            //原料数最小化
            val maxUseMaterialNum = requirement.maxUseMaterialNum
            val materialsCount = recipeMaterials.filter { it.value.weight.value > 0.0 }.count()
            if ((maxUseMaterialNum == null || maxUseMaterialNum != materialsCount) && minMaterialNum) {
                //固定成本
                objectiveVars.eq(recipe.cost)

                //使用最小数量原料
                recipeMaterials.values.map {
                    val intVar = intVar(0.0, 1.0)
                    intVar.geExpr(1.0)
                        .onlyEnforceIf(it.weight.gtExpr(0.0))
                    intVar
                }.minimize()
                setTimeLimit(10)
                solve()
                if (!isOptimal()) {
                    log.warn("${requirement.id}:${solver.name} ${solver.epsilon} Could not find optimal solution:${getResultStatus()}")
                } else {
                    if (log.isDebugEnabled) {
                        log.debug("${requirement.id}:${solver.name} ${solver.epsilon} find optimal solution:${getResultStatus()}")
                    }
                    val minMaterialsCount =
                        recipeMaterials.filter { it.value.weight.value > 0.0 }.count()
                    if (materialsCount > minMaterialsCount)
                        recipe = recipe(recipeName, minEpsilon, recipe.cost)
                }
            }
            return recipe
        }
    }

    private fun Solver.recipe(
        recipeName: String?,
        minEpsilon: Double,
        objectiveValue: Double,
    ): Recipe {
        val scale = abs(log10(epsilon)).toInt()
        val materials = recipeMaterials.mapNotNull { (_, u) ->
            val solverValue = u.weight.value
            val value = solverValue.scale(scale)
            if (value != 0.0) {
                u.toMaterialValue()
            } else {
                null
            }
        }
        return Recipe(
            recipeName = recipeName ?: defaultRecipeName,
            requirement = requirement,
            includeProductionCost = includeProductionCost,
            optimalProductionCost = requirement.productionCost.computeFee(
                materialItems = materialItems?.map {
                    CarrierValue(
                        it.it,
                        it.value.value
                    )
                },
                dictItems = dictItems?.mapValues {
                    CarrierValue(
                        it.value.it,
                        it.value.value.value
                    )
                },
                scale = scale,
                minEpsilon = minEpsilon
            ),
            cost = objectiveValue,
            materials = materials,
            scale = scale,
            indicatorScale = requirement.indicatorScale,
            minEpsilon = minEpsilon
        )
    }

}