package top.bettercode.summer.tools.recipe

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale
import top.bettercode.summer.tools.optimal.solver.Solver
import top.bettercode.summer.tools.optimal.solver.SolverFactory
import top.bettercode.summer.tools.optimal.solver.SolverType
import top.bettercode.summer.tools.optimal.solver.`var`.IVar
import top.bettercode.summer.tools.recipe.criteria.Operator
import top.bettercode.summer.tools.recipe.material.MaterialIDs
import top.bettercode.summer.tools.recipe.material.RecipeMaterialVar
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial
import top.bettercode.summer.tools.recipe.productioncost.*
import top.bettercode.summer.tools.recipe.result.Recipe

object RecipeSolver {

    private val log: Logger = LoggerFactory.getLogger(RecipeSolver::class.java)

    fun solve(solverType: SolverType, requirement: RecipeRequirement, includeProductionCost: Boolean = true): Recipe? {
        SolverFactory.createSolver(solverType = solverType, dub = requirement.targetWeight).apply {
            val s = System.currentTimeMillis()
            val (recipeMaterials, objective) = prepare(requirement, includeProductionCost)
            if (SolverType.COPT == solverType) {
                val numVariables = numVariables()
                val numConstraints = numConstraints()
                log.info("变量数量：{},约束数量：{}", numConstraints, numConstraints)
                if (numVariables > 2000 || numConstraints > 2000) {
                    log.error("变量或约束过多，变量数量：$numVariables 约束数量：$numConstraints")
                }
            }
            // 求解
            solve()
            val e = System.currentTimeMillis()
            log.info("${requirement.productName}求解耗时：" + (e - s) + "ms")

            if (isOptimal()) {
                return Recipe(requirement = requirement, includeProductionCost = includeProductionCost, cost = objective.value.scale(),
                        materials = recipeMaterials.mapNotNull { (_, u) ->
                            val value = u.weight.value
                            if (value != 0.0) {
                                u.toMaterialValue()
                            } else {
                                null
                            }
                        })
            }
            return null
        }
    }

    fun Solver.prepare(requirement: RecipeRequirement, includeProductionCost: Boolean = true): Pair<Map<String, RecipeMaterialVar>, IVar> {

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
        for (notMixedMaterial in notMixMaterials) {
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
        materialVars.between(targetWeight, if (requirement.maxBakeWeight == null) Double.POSITIVE_INFINITY else targetWeight + requirement.maxBakeWeight)

        val rangeIndicators = requirement.indicatorRangeConstraints
        // 产品水分指标
        val waterRange = rangeIndicators.productWater?.value
        // 定义产品干净重
        val minDryWeight = targetWeight * (1 - (waterRange?.max ?: 0.0))
        val maxDryWeight = targetWeight * (1 - (waterRange?.min ?: 0.0))
        recipeMaterials.map {
            val material = it.value
            it.value.weight * (1 - material.indicators.waterValue)
        }.between(minDryWeight, maxDryWeight)

        // 添加成份约束条件
        // 成份要求 总养分 氮含量 磷含量 水溶磷率 钾含量 氯离子 产品水分 物料水分 硼 锌
        for (indicator in rangeIndicators) {
            val range = indicator.value
            if (indicator.isProductWater) {
                continue
            }
            if (indicator.isRateToOther) {
                val otherVar = recipeMaterials.map {
                    val material = it.value
                    val coeff = material.indicators.valueOf(indicator.otherId!!)
                    it.value.weight * coeff
                }.sum()

                val itVar = recipeMaterials.map {
                    val material = it.value
                    val coeff = material.indicators.valueOf(indicator.itId!!)
                    it.value.weight * coeff
                }.sum()

                val minRate = indicator.value.min
                val maxRate = indicator.value.max
                itVar.ratioInRange(otherVar, minRate, maxRate)
            } else {
                recipeMaterials.map {
                    val material = it.value
                    val coeff = material.indicators.valueOf(indicator.id)
                    material.weight * coeff
                }.between(targetWeight * range.min, targetWeight * range.max)
            }
        }


        // 原料用量
        val materialRangeConstraints = requirement.materialRangeConstraints
        materialRangeConstraints.forEach { (t, u) ->
            t.mapNotNull { recipeMaterials[it]?.weight }
                    .between(u.min, u.max)
        }

        // 关联原料比率约束
        val materialRelationConstraints = requirement.materialRelationConstraints
        materialRelationConstraints.forEach { (ids, relation) ->
            val consumeMaterialVars = ids.mapNotNull { recipeMaterials[it] }
            val replaceConsumeMaterialVars = ids.replaceIds?.mapNotNull { recipeMaterials[it] }
            val normalVars = mutableListOf<IVar>()
            val overdoseVars = mutableListOf<IVar>()
            val useReplace = boolVar()

            //关联原料
            val normalMinVars = mutableListOf<IVar>()
            val normalMaxVars = mutableListOf<IVar>()
            val overdoseMinVars = mutableListOf<IVar>()
            val overdoseMaxVars = mutableListOf<IVar>()

            relation.forEach { (t, u) ->
                val normal = u.normal
                val overdose = u.overdose
                val overdoseMaterial = u.overdoseMaterial
                val relationIds = t.relationIds
                t.mapNotNull { recipeMaterials[it] }.forEach { m ->
                    //原料消耗变量初始化
                    consumeMaterialVars.forEach {
                        it.weight.leIf(0.0, useReplace)
                        val normalVar = numVar(0.0, targetWeight)
                        normalVars.add(normalVar)
                        val overdoseVar = numVar(0.0, targetWeight)
                        overdoseVars.add(overdoseVar)
                        it.consumes[m.id] = normalVar to overdoseVar
                    }
                    //替换原料消耗变量初始化
                    replaceConsumeMaterialVars?.forEach {
                        val replaceRate = ids.replaceRate!!
                        it.weight.leIfNot(0.0, useReplace)
                        val normalVar = numVar(0.0, targetWeight)
                        normalVars.add(normalVar  / replaceRate)
                        val overdoseVar = numVar(0.0, targetWeight)
                        overdoseVars.add(overdoseVar / replaceRate)
                        it.consumes[m.id] = normalVar to overdoseVar
                    }

                    //m 对应原料的用量变量
                    val normalWeight =
                            if (m.consumes.isEmpty()) {//当无其他消耗m原料时，取本身用量
                                m.weight
                            } else if (relationIds == null) {//当有其他消耗原料时且关联原料不存在，如：硫酸量耗液氨，取所有消耗汇总
                                m.consumes.values.map { it.first }.sum()
                            } else {//当有其他消耗原料时且关联原料存在，如：氯化钾反应所需硫酸量耗液氨,取关联原料消耗汇总
                                m.consumes.filterKeys { relationIds.contains(it) }.values.map { it.first }.sum()
                            }
                    val overdoseWeight =
                            if (m.consumes.isEmpty()) {//当无其他消耗m原料时，不存在过量消耗
                                null
                            } else if (relationIds == null) {//当有其他消耗原料时且关联原料不存在，如：硫酸量耗液氨，取所有消耗汇总
                                m.consumes.values.map { it.second }.sum()
                            } else {//当有其他消耗原料时且关联原料存在，如：氯化钾反应所需硫酸量耗液氨,取关联原料消耗汇总
                                m.consumes.filterKeys { relationIds.contains(it) }.values.map { it.second }.sum()
                            }

                    //原料消耗
                    normalMinVars.add(normalWeight * normal.min)
                    normalMaxVars.add(normalWeight * normal.max)
                    if (overdose != null) {
                        overdoseMinVars.add(normalWeight * overdose.min)
                        overdoseMaxVars.add(normalWeight * overdose.max)
                    }

                    // 过量原料
                    if (overdoseMaterial != null && overdoseWeight != null) {
                        val overdoseMaterialNormal = overdoseMaterial.normal
                        val overdoseMaterialOverdose = overdoseMaterial.overdose
                        //过量原料消耗
                        normalMinVars.add(overdoseWeight * overdoseMaterialNormal.min)
                        normalMaxVars.add(overdoseWeight * overdoseMaterialNormal.max)
                        //过量原料过量消耗
                        if (overdoseMaterialOverdose != null) {
                            overdoseMinVars.add(overdoseWeight * overdoseMaterialOverdose.min)
                            overdoseMaxVars.add(overdoseWeight * overdoseMaterialOverdose.max)
                        }
                    }
                }
            }
            consumeMaterialVars.forEach {
                it.consumes.values.flatMap { c -> listOf(c.first, c.second) }.sum().eq(it.weight)
            }
            replaceConsumeMaterialVars?.forEach {
                it.consumes.values.flatMap { c -> listOf(c.first, c.second) }.sum().eq(it.weight)
            }
            normalVars.between(normalMinVars.sum(), normalMaxVars.sum())
            overdoseVars.between(overdoseMinVars.sum(), overdoseMaxVars.sum())
        }


        // 条件约束
        requirement.materialConditionConstraints.forEach { (whenCondition, thenCondition) ->
            val whenVar = whenCondition.materials.mapNotNull { recipeMaterials[it]?.weight }.sum()
            val thenVar = thenCondition.materials.mapNotNull { recipeMaterials[it]?.weight }.sum()
            val boolVar = boolVar()
            val whenCon = whenCondition.condition
            when (whenCon.operator) {
                Operator.EQ -> {
                    whenVar.neIfNot(whenCon.value, boolVar)
                }

                Operator.NE -> {
                    whenVar.eqIfNot(whenCon.value, boolVar)
                }

                Operator.GT -> {
                    whenVar.leIfNot(whenCon.value, boolVar)
                }

                Operator.LT -> {
                    whenVar.geIfNot(whenCon.value, boolVar)
                }

                Operator.GE -> {
                    whenVar.ltIfNot(whenCon.value, boolVar)
                }

                Operator.LE -> {
                    whenVar.gtIfNot(whenCon.value, boolVar)
                }
            }
            val thenCon = thenCondition.condition
            when (thenCon.operator) {
                Operator.EQ -> thenVar.eqIf(thenCon.value, boolVar)
                Operator.NE -> thenVar.neIf(thenCon.value, boolVar)
                Operator.GT -> thenVar.gtIf(thenCon.value, boolVar)
                Operator.LT -> thenVar.ltIf(thenCon.value, boolVar)
                Operator.GE -> thenVar.geIf(thenCon.value, boolVar)
                Operator.LE -> thenVar.leIf(thenCon.value, boolVar)
            }
        }

        //制造费用
        val objectiveVarList = if (includeProductionCost) {
            val productionCost = requirement.productionCost
            val materialItems = productionCost.materialItems.map { CarrierValue(it, numVar(1.0, 1.0)) }
            val dictItems = productionCost.dictItems.mapValues { CarrierValue(it.value, numVar(1.0, 1.0)) }
            //费用增减
            var allChange = 1.0
            productionCost.changes.forEach { changeLogic ->
                when (changeLogic.type) {
                    ChangeLogicType.WATER_OVER -> {
                        changeProductionCost(recipeMaterials, changeLogic, recipeMaterials.map {
                            it.value.weight * it.value.indicators.waterValue
                        }.sum(), materialItems, dictItems)
                    }

                    ChangeLogicType.OVER -> {
                        changeProductionCost(recipeMaterials, changeLogic, null, materialItems, dictItems)
                    }

                    ChangeLogicType.OTHER -> allChange += changeLogic.changeValue
                }
            }
            //能耗费用
            val energyFee = materialItems.map { it.value * it.it.cost }.sum()
            //人工+折旧费+其他费用
            val otherFee = dictItems.values.map { it.value * it.it.cost }.sum()
            //税费 =（人工+折旧费+其他费用）*0.09+15
            val taxFee = otherFee * productionCost.taxRate + productionCost.taxFloat
            val fee = arrayOf(energyFee, otherFee, taxFee).sum() * allChange

            recipeMaterials.values.map {
                it.weight * it.price
            } + fee
        } else {
            recipeMaterials.values.map {
                it.weight * it.price
            }
        }
        // 定义目标函数：最小化成本
        val objective = objectiveVarList.minimize()

        return recipeMaterials to objective
    }

    private fun Solver.changeProductionCost(materials: Map<String, RecipeMaterialVar>, changeLogic: CostChangeLogic, value: IVar?, materialItems: List<CarrierValue<RecipeOtherMaterial, IVar>>, dictItems: Map<DictType, CarrierValue<Cost, IVar>>) {
        val useMaterial = materials[changeLogic.materialId]
        if (useMaterial != null) {
            val boolVar = boolVar()
            //boolVar true <=0
            useMaterial.weight.gtIfNot(0.0, boolVar)
            changeLogic.changeItems!!.forEach { item ->
                when (item.type) {
                    ChangeItemType.MATERIAL -> {//能耗费用
                        //(1+(value-exceedValue)/eachValue*changeValue)
                        val material = materialItems.find { it.it.id == item.id }
                        if (material != null) {
                            //change
                            val changeVar = ((value
                                    ?: useMaterial.weight) - changeLogic.exceedValue!!) * (changeLogic.changeValue / changeLogic.eachValue!!)
                            changeVar.leIf(0.0, boolVar)
                            material.value += changeVar
                        }
                    }

                    ChangeItemType.DICT -> {
                        when (val dictType = DictType.valueOf(item.id)) {
                            DictType.ENERGY -> {
                                materialItems.forEach {
                                    //change
                                    val changeVar = ((value
                                            ?: useMaterial.weight) - changeLogic.exceedValue!!) / changeLogic.eachValue!! * changeLogic.changeValue
                                    changeVar.leIf(0.0, boolVar)
                                    it.value += changeVar
                                }
                            }

                            else -> {
                                val cost = dictItems[dictType]
                                if (cost != null) {
                                    //change
                                    val changeVar = ((value
                                            ?: useMaterial.weight) - changeLogic.exceedValue!!) / changeLogic.eachValue!! * changeLogic.changeValue
                                    changeVar.leIf(0.0, boolVar)
                                    cost.value += changeVar
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
