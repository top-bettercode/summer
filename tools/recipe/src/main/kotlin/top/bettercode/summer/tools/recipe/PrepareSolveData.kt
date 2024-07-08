package top.bettercode.summer.tools.recipe

import org.slf4j.LoggerFactory
import org.springframework.util.Assert
import top.bettercode.summer.tools.optimal.Expr
import top.bettercode.summer.tools.optimal.IVar
import top.bettercode.summer.tools.optimal.Operator
import top.bettercode.summer.tools.optimal.OptimalUtil.scale
import top.bettercode.summer.tools.optimal.Solver
import top.bettercode.summer.tools.recipe.criteria.UsageVar
import top.bettercode.summer.tools.recipe.indicator.IndicatorUnit
import top.bettercode.summer.tools.recipe.material.RecipeMaterialVar
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs
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
            includeProductionCost: Boolean = true
        ): PrepareSolveData {
            solver.apply {
                setTimeLimit(requirement.timeout)
                // 原料数量
                val materials = requirement.materials
                val numRawMaterials = materials.size
                val numMaxMaterials = requirement.maxUseMaterialNum
                val targetWeight = requirement.targetWeight

                val recipeMaterials = materials.associateBy { it.id }.mapValues {
                    RecipeMaterialVar(it.value, numVar(0.0, targetWeight))
                }

                val materialVars = recipeMaterials.values.map { it.weight }

                // 进料口数量
                if (numMaxMaterials != null && numMaxMaterials in 1 until numRawMaterials) {
                    materialVars.atMost(numMaxMaterials)
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

                        indicator.isRateToOther -> {
                            val otherVar = recipeMaterials.map {
                                val material = it.value
                                val coeff = material.indicators.valueOf(indicator.otherId!!)
                                material.weight * coeff
                            }.sum()

                            val itVar = recipeMaterials.map {
                                val material = it.value
                                val coeff = material.indicators.valueOf(indicator.itId!!)
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
                materialRelationConstraints.forEach { (ids, relation) ->
                    val consumeMaterialVars = ids.mapNotNull { recipeMaterials[it] }
                    val replaceConsumeMaterialVars =
                        ids.replaceIds?.mapNotNull { recipeMaterials[it] }
                    val useReplace = boolVar()

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
                                it.weight.leIf(0.0, useReplace)
                                val normalVar = numVar(0.0, targetWeight)
                                normalVars.add(normalVar)
                                val overdoseVar = numVar(0.0, targetWeight)
                                overdoseVars.add(overdoseVar)
                                it.consumes[m.id] =
                                    UsageVar(normal = normalVar, overdose = overdoseVar)
                            }
                            //替换原料消耗变量初始化
                            replaceConsumeMaterialVars?.forEach {
                                val replaceRate = ids.replaceRate!!
                                it.weight.leIfNot(0.0, useReplace)
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
                                    Assert.notEmpty(
                                        m.consumes,
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
                                    Assert.notEmpty(
                                        m.consumes,
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
                    materialItems =
                        productionCost.materialItems.map { CarrierValue(it, numVar(1.0, 1.0)) }
                    dictItems =
                        productionCost.dictItems.mapValues {
                            CarrierValue(
                                it.value,
                                numVar(1.0, 1.0)
                            )
                        }
                    //费用增减
                    var allChange = 1.0
                    productionCost.changes.forEach { changeLogic ->
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
                        if (value.lb < 0.0) {
                            value.lb = 0.0
                        }
                        value * it.it.cost
                    }.sum()
                    //人工+折旧费+其他费用
                    val otherFee = dictItems.values.map {
                        val value = it.value
                        if (value.lb < 0.0) {
                            value.lb = 0.0
                        }
                        value * it.it.cost
                    }.sum()
                    //税费 =（人工+折旧费+其他费用）*0.09+15
                    val taxFee = otherFee * productionCost.taxRate + productionCost.taxFloat
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
            dictItems: Map<DictType, CarrierValue<Cost, IVar>>
        ) {
            val iVars = materials.filter { changeLogic.materialId?.contains(it.key) == true }
                .map { it.value.weight }
            if (iVars.isNotEmpty()) {
                val useMaterial = iVars.sum()
                val thens = mutableListOf<Expr>()
                changeLogic.changeItems!!.forEach { item ->
                    when (item.type) {
                        ChangeItemType.MATERIAL -> {//能耗费用
                            //(1+(value-exceedValue)/eachValue*changeValue)
                            val material = materialItems.find { it.it.id == item.id }
                            if (material != null) {
                                //change
                                val changeVar = ((value
                                    ?: useMaterial) - changeLogic.exceedValue!!) * (changeLogic.changeValue / changeLogic.eachValue!!)
                                if (changeWhenMaterialUsed)
                                    thens.add(changeVar.eqExpr(0.0))
                                material.value += changeVar
                            }
                        }

                        ChangeItemType.DICT -> {
                            when (val dictType = DictType.valueOf(item.id)) {
                                DictType.ENERGY -> {
                                    materialItems.forEach {
                                        //change
                                        val changeVar = ((value
                                            ?: useMaterial) - changeLogic.exceedValue!!) * (changeLogic.changeValue / changeLogic.eachValue!!)
                                        if (changeWhenMaterialUsed)
                                            thens.add(changeVar.eqExpr(0.0))
                                        it.value += changeVar
                                    }
                                }

                                else -> {
                                    val cost = dictItems[dictType]
                                    if (cost != null) {
                                        //change
                                        val changeVar = ((value
                                            ?: useMaterial) - changeLogic.exceedValue!!) * (changeLogic.changeValue / changeLogic.eachValue!!)
                                        if (changeWhenMaterialUsed)
                                            thens.add(changeVar.eqExpr(0.0))
                                        cost.value += changeVar
                                    }
                                }
                            }
                        }
                    }
                }
                if (changeWhenMaterialUsed)
                //当使用原料为0时，changeVar=0.0
                    thens.onlyEnforceIf(useMaterial.leExpr(0.0))
            }
        }
    }

    @JvmOverloads
    fun solve(
        solver: Solver,
        minMaterialNum: Boolean,
        recipeName: String? = null,
        minEpsilon: Double
    ): Recipe? {
        solver.apply {
            val minimize = objectiveVars.minimize()
            solve()
            var recipe: Recipe
            if (!isOptimal()) {
                log.warn("${solver.name} ${solver.epsilon} Could not find optimal solution:${getResultStatus()}")
                return null
            } else {
                if (log.isDebugEnabled) {
                    log.debug("${solver.name} ${solver.epsilon} find optimal solution:${getResultStatus()}")
                }
                recipe = recipe(recipeName, minEpsilon, minimize.value)
            }

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
                solve()
                if (!isOptimal()) {
                    log.warn("${solver.name} ${solver.epsilon} Could not find optimal solution:${getResultStatus()}")
                } else {
                    if (log.isDebugEnabled) {
                        log.debug("${solver.name} ${solver.epsilon} find optimal solution:${getResultStatus()}")
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
        objectiveValue: Double
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