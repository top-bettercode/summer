package top.bettercode.summer.tools.recipe

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale
import top.bettercode.summer.tools.optimal.solver.Solver
import top.bettercode.summer.tools.optimal.solver.`var`.IVar
import top.bettercode.summer.tools.recipe.criteria.Operator
import top.bettercode.summer.tools.recipe.material.MaterialIDs
import top.bettercode.summer.tools.recipe.material.RecipeMaterialVar
import top.bettercode.summer.tools.recipe.material.SolutionVar

object RecipeSolver {

    private val log: Logger = LoggerFactory.getLogger(RecipeSolver::class.java)

    fun solve(solver: Solver, requirement: RecipeRequirement): Recipe? {
        solver.apply {
            val s = System.currentTimeMillis()
            val (recipeMaterials, objective) = prepare(requirement)
            // 求解
            solve()
            val e = System.currentTimeMillis()
            log.info("${requirement.productName}求解耗时：" + (e - s) + "ms")
            if (numVariables() > 2000 || numConstraints() > 2000) {
                log.error("变量或约束过多，变量数量：" + numVariables() + " 约束数量：" + numConstraints())
                return null
            }
            if (isOptimal()) {
                return Recipe(requirement, objective.value.scale(),
                        recipeMaterials.mapNotNull { (_, u) ->
                            val value = u.solutionVar.value
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
        val numMaxMaterials = requirement.maxUseMaterials
        val targetWeight = requirement.targetWeight

        val recipeMaterials = materials.mapValues {
            RecipeMaterialVar(it.value, SolutionVar(delegate = numVar(0.0, targetWeight)))
        }

        val materialVars = recipeMaterials.values.map { it.solutionVar }.toTypedArray()

        // 进料口数量
        if (numMaxMaterials in 1 until numRawMaterials) {
            materialVars.atMost(numMaxMaterials)
        }

        // 不能混用的原料
        val notMixMaterials = requirement.notMixMaterials

        // 不选取不能同时使用的原料对
        for (notMixedMaterial in notMixMaterials) {
            //一组变量至多有一个变量可取非零值
            val noMixedVars = notMixedMaterial
                    .map { materialIDs: MaterialIDs ->
                        val vars = materialIDs.mapNotNull { recipeMaterials[it]?.solutionVar }.toTypedArray()
                        if (vars.isEmpty()) return@map null
                        vars.sum()
                    }.filterNotNull().toTypedArray()
            noMixedVars.atMostOne()
        }


        // 定义产品净重 >=1000kg，含水，最大烘干量
        materialVars.between(targetWeight, if (requirement.maxBakeWeight < 0) Double.POSITIVE_INFINITY else targetWeight + requirement.maxBakeWeight)

        val rangeIndicators = requirement.rangeIndicators
        // 水分
        val waterRange = rangeIndicators.water?.value
        // 定义产品干净重
        val minDryWeight = targetWeight * (1 - (waterRange?.max ?: 0.0))
        val maxDryWeight = targetWeight * (1 - (waterRange?.min ?: 0.0))
        recipeMaterials.map {
            val material = it.value
            val indicators = material.indicators
            it.value.solutionVar.coeff(1 - indicators.waterValue)
        }.toTypedArray()
                .between(minDryWeight, maxDryWeight)

        // 添加成份约束条件
        // 成份要求 总养分 氮含量 磷含量 水溶磷率 钾含量 氯离子 水分 硼 锌
        for (indicator in rangeIndicators) {
            val range = indicator.value
            if (indicator.isWater) {
                continue
            }
            if (indicator.isRateToOther) {
                val otherVar = recipeMaterials.map {
                    val material = it.value
                    val coeff = material.indicators.valueOf(indicator.otherIndex!!)
                    it.value.solutionVar.coeff(coeff)
                }.toTypedArray().sum()

                val itVar = recipeMaterials.map {
                    val material = it.value
                    val coeff = material.indicators.valueOf(indicator.itIndex!!)
                    it.value.solutionVar.coeff(coeff)
                }.toTypedArray().sum()

                val minRate = indicator.value.min
                val maxRate = indicator.value.max
                itVar.ratioInRange(otherVar, minRate, maxRate)
            } else {
                recipeMaterials.map {
                    val material = it.value
                    val indicators = material.indicators
                    val coeff = indicators.valueOf(indicator.index)
                    material.solutionVar.coeff(coeff)
                }.toTypedArray()
                        .between(targetWeight * range.min, targetWeight * range.max)
            }
        }


        // 原料用量
        val materialRangeConstraints = requirement.materialRangeConstraints
        materialRangeConstraints.forEach { (t, u) ->
            t.mapNotNull { recipeMaterials[it]?.solutionVar }
                    .toTypedArray()
                    .between(u.min, u.max)
        }


        // 原料比率约束
        val materialRelationConstraints = requirement.materialRelationConstraints
        materialRelationConstraints.forEach { (ids, relation) ->
            val normalVars = mutableListOf<IVar>()
            val overdoseVars = mutableListOf<IVar>()
            val useReplace = boolVar()
            ids.mapNotNull { recipeMaterials[it] }.forEach {
                it.solutionVar.eqIf(0.0, useReplace)
                val normalVar = numVar(0.0, targetWeight)
                it.solutionVar.normalDelegate = normalVar
                normalVars.add(normalVar)
                val overdoseVar = numVar(0.0, targetWeight)
                it.solutionVar.overdoseDelegate = overdoseVar
                overdoseVars.add(overdoseVar)
                arrayOf(normalVar, overdoseVar).eq(it.solutionVar)
            }

            ids.replaceIds?.mapNotNull { recipeMaterials[it] }?.forEach {
                val replaceRate = ids.replaceRate!!
                it.solutionVar.eqIfNot(0.0, useReplace)
                val normalVar = numVar(0.0, targetWeight)
                it.solutionVar.normalDelegate = normalVar
                normalVars.add(normalVar.coeff(1 / replaceRate))
                val overdoseVar = numVar(0.0, targetWeight)
                it.solutionVar.overdoseDelegate = overdoseVar
                overdoseVars.add(overdoseVar.coeff(1 / replaceRate))
                arrayOf(normalVar, overdoseVar).eq(it.solutionVar)
            }

            //关联物料
            val normalMinVars = mutableListOf<IVar>()
            val normalMaxVars = mutableListOf<IVar>()
            val overdoseMinVars = mutableListOf<IVar>()
            val overdoseMaxVars = mutableListOf<IVar>()

            relation.forEach { (t, u) ->
                val normal = u.normal
                val overdose = u.overdose
                t.mapNotNull { recipeMaterials[it]?.solutionVar }.forEach {
                    val normalDelegate = it.normalDelegate
                    if (normalDelegate != null) {
                        normalMinVars.add(normalDelegate.coeff(normal.min))
                        normalMaxVars.add(normalDelegate.coeff(normal.max))
                    } else {
                        normalMinVars.add(it.coeff(normal.min))
                        normalMaxVars.add(it.coeff(normal.max))
                    }
                    if (overdose != null) {
                        val overdoseDelegate = it.overdoseDelegate
                        if (overdoseDelegate != null) {
                            overdoseMinVars.add(overdoseDelegate.coeff(overdose.min))
                            overdoseMaxVars.add(overdoseDelegate.coeff(overdose.max))
                        } else {
                            overdoseMinVars.add(it.coeff(overdose.min))
                            overdoseMaxVars.add(it.coeff(overdose.max))
                        }
                    }
                }
            }
            normalVars.toTypedArray().between(normalMinVars.toTypedArray().sum(), normalMaxVars.toTypedArray().sum())
            overdoseVars.toTypedArray().between(overdoseMinVars.toTypedArray().sum(), overdoseMaxVars.toTypedArray().sum())
        }


        // 条件约束
        requirement.materialConditions.forEach { (whenCondition, thenCondition) ->
            val whenVar = whenCondition.materials.mapNotNull { recipeMaterials[it]?.solutionVar }.toTypedArray().sum()
            val thenVar = thenCondition.materials.mapNotNull { recipeMaterials[it]?.solutionVar }.toTypedArray().sum()
            val boolVar = boolVar()
            val whenCon = whenCondition.condition
            when (whenCon.operator) {
                Operator.EQUAL -> {
                    whenVar.neIfNot(whenCon.value, boolVar)
                }

                Operator.NOT_EQUAL -> {
                    whenVar.eqIfNot(whenCon.value, boolVar)
                }

                Operator.GREATER -> {
                    whenVar.leIfNot(whenCon.value, boolVar)
                }

                Operator.LESS -> {
                    whenVar.geIfNot(whenCon.value, boolVar)
                }

                Operator.GREATER_EQUAL -> {
                    whenVar.ltIfNot(whenCon.value, boolVar)
                }

                Operator.LESS_EQUAL -> {
                    whenVar.gtIfNot(whenCon.value, boolVar)
                }
            }
            val thenCon = thenCondition.condition
            when (thenCon.operator) {
                Operator.EQUAL -> thenVar.eqIf(thenCon.value, boolVar)
                Operator.NOT_EQUAL -> thenVar.neIf(thenCon.value, boolVar)
                Operator.GREATER -> thenVar.gtIf(thenCon.value, boolVar)
                Operator.LESS -> thenVar.ltIf(thenCon.value, boolVar)
                Operator.GREATER_EQUAL -> thenVar.geIf(thenCon.value, boolVar)
                Operator.LESS_EQUAL -> thenVar.leIf(thenCon.value, boolVar)
            }
        }

        // 定义目标函数：最小化成本
        val objective = recipeMaterials.values.map {
            it.solutionVar.coeff(it.price)
        }.toTypedArray().minimize()

        return recipeMaterials to objective
    }

}
