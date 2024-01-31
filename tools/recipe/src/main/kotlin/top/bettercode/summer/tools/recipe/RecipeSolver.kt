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
import top.bettercode.summer.tools.recipe.result.Recipe

object RecipeSolver {

    private val log: Logger = LoggerFactory.getLogger(RecipeSolver::class.java)

    fun solve(solverType: SolverType, requirement: RecipeRequirement): Recipe? {
        SolverFactory.createSolver(solverType = solverType, dub = requirement.targetWeight).apply {
            val s = System.currentTimeMillis()
            val (recipeMaterials, objective) = prepare(requirement)
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
                return Recipe(requirement, objective.value.scale(),
                        recipeMaterials.mapNotNull { (_, u) ->
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

    fun Solver.prepare(requirement: RecipeRequirement): Pair<Map<String, RecipeMaterialVar>, IVar> {

        setTimeLimit(requirement.timeout)
        // 物料数量
        val materials = requirement.materials
        val numRawMaterials = materials.size
        val numMaxMaterials = requirement.maxMaterialNum
        val targetWeight = requirement.targetWeight

        val recipeMaterials = materials.mapValues {
            RecipeMaterialVar(it.value, numVar(0.0, targetWeight))
        }

        val materialVars = recipeMaterials.values.map { it.weight }

        // 进料口数量
        if (numMaxMaterials in 1 until numRawMaterials) {
            materialVars.atMost(numMaxMaterials)
        }

        // 不能混用的物料
        val notMixMaterials = requirement.notMixMaterials

        // 不选取不能同时使用的物料对
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
        materialVars.between(targetWeight, if (requirement.maxBakeWeight < 0) Double.POSITIVE_INFINITY else targetWeight + requirement.maxBakeWeight)

        val rangeIndicators = requirement.rangeIndicators
        // 产品水分指标
        val waterRange = rangeIndicators.productWater?.value
        // 定义产品干净重
        val minDryWeight = targetWeight * (1 - (waterRange?.max ?: 0.0))
        val maxDryWeight = targetWeight * (1 - (waterRange?.min ?: 0.0))
        recipeMaterials.map {
            val material = it.value
            val indicators = material.indicators
            it.value.weight.coeff(1 - indicators.waterValue)
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
                    it.value.weight.coeff(coeff)
                }.sum()

                val itVar = recipeMaterials.map {
                    val material = it.value
                    val coeff = material.indicators.valueOf(indicator.itId!!)
                    it.value.weight.coeff(coeff)
                }.sum()

                val minRate = indicator.value.min
                val maxRate = indicator.value.max
                itVar.ratioInRange(otherVar, minRate, maxRate)
            } else {
                recipeMaterials.map {
                    val material = it.value
                    val indicators = material.indicators
                    val coeff = indicators.valueOf(indicator.id)
                    material.weight.coeff(coeff)
                }.between(targetWeight * range.min, targetWeight * range.max)
            }
        }


        // 物料用量
        val materialRangeConstraints = requirement.materialRangeConstraints
        materialRangeConstraints.forEach { (t, u) ->
            t.mapNotNull { recipeMaterials[it]?.weight }
                    .between(u.min, u.max)
        }


        // 关联物料比率约束
        val materialRelationConstraints = requirement.materialRelationConstraints
        materialRelationConstraints.forEach { (ids, relation) ->
            val consumeMaterialVars = ids.mapNotNull { recipeMaterials[it] }
            val replaceConsumeMaterialVars = ids.replaceIds?.mapNotNull { recipeMaterials[it] }
            val normalVars = mutableListOf<IVar>()
            val overdoseVars = mutableListOf<IVar>()
            val useReplace = boolVar()

            //关联物料
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
                    //物料消耗变量初始化
                    consumeMaterialVars.forEach {
                        it.weight.eqIf(0.0, useReplace)
                        val normalVar = numVar(0.0, targetWeight)
                        normalVars.add(normalVar)
                        val overdoseVar = numVar(0.0, targetWeight)
                        overdoseVars.add(overdoseVar)
                        it.consumes[m.id] = normalVar to overdoseVar
                    }
                    //替换物料消耗变量初始化
                    replaceConsumeMaterialVars?.forEach {
                        val replaceRate = ids.replaceRate!!
                        it.weight.eqIfNot(0.0, useReplace)
                        val normalVar = numVar(0.0, targetWeight)
                        normalVars.add(normalVar.coeff(1 / replaceRate))
                        val overdoseVar = numVar(0.0, targetWeight)
                        overdoseVars.add(overdoseVar.coeff(1 / replaceRate))
                        it.consumes[m.id] = normalVar to overdoseVar
                    }

                    //m 对应原料的用量变量
                    val normalWeight =
                            if (m.consumes.isEmpty()) {//当无其他消耗m物料时，取本身用量
                                m.weight
                            } else if (relationIds == null) {//当有其他消耗物料时且关联物料不存在，如：硫酸量耗液氨，取所有消耗汇总
                                m.consumes.values.map { it.first }.sum()
                            } else {//当有其他消耗物料时且关联物料存在，如：氯化钾反应所需硫酸量耗液氨,取关联物料消耗汇总
                                m.consumes.filterKeys { relationIds.contains(it) }.values.map { it.first }.sum()
                            }
                    val overdoseWeight =
                            if (m.consumes.isEmpty()) {//当无其他消耗m物料时，不存在过量消耗
                                null
                            } else if (relationIds == null) {//当有其他消耗物料时且关联物料不存在，如：硫酸量耗液氨，取所有消耗汇总
                                m.consumes.values.map { it.second }.sum()
                            } else {//当有其他消耗物料时且关联物料存在，如：氯化钾反应所需硫酸量耗液氨,取关联物料消耗汇总
                                m.consumes.filterKeys { relationIds.contains(it) }.values.map { it.second }.sum()
                            }

                    //物料消耗
                    normalMinVars.add(normalWeight.coeff(normal.min))
                    normalMaxVars.add(normalWeight.coeff(normal.max))
                    if (overdose != null) {
                        overdoseMinVars.add(normalWeight.coeff(overdose.min))
                        overdoseMaxVars.add(normalWeight.coeff(overdose.max))
                    }

                    // 过量物料
                    if (overdoseMaterial != null && overdoseWeight != null) {
                        val overdoseMaterialNormal = overdoseMaterial.normal
                        val overdoseMaterialOverdose = overdoseMaterial.overdose
                        //过量物料消耗
                        normalMinVars.add(overdoseWeight.coeff(overdoseMaterialNormal.min))
                        normalMaxVars.add(overdoseWeight.coeff(overdoseMaterialNormal.max))
                        //过量物料过量消耗
                        if (overdoseMaterialOverdose != null) {
                            overdoseMinVars.add(overdoseWeight.coeff(overdoseMaterialOverdose.min))
                            overdoseMaxVars.add(overdoseWeight.coeff(overdoseMaterialOverdose.max))
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
        requirement.materialConditions.forEach { (whenCondition, thenCondition) ->
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

        // 定义目标函数：最小化成本
        val objective = recipeMaterials.values.map {
            it.weight.coeff(it.price)
        }.minimize()

        return recipeMaterials to objective
    }

}
