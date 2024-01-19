package top.bettercode.summer.tools.recipe

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.optimal.solver.Solver
import top.bettercode.summer.tools.optimal.solver.`var`.IVar
import top.bettercode.summer.tools.recipe.criteria.Operator
import top.bettercode.summer.tools.recipe.data.RecipeResult
import top.bettercode.summer.tools.recipe.material.MaterialIDs
import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue
import top.bettercode.summer.tools.recipe.material.RecipeMaterialVar
import top.bettercode.summer.tools.recipe.material.SolutionVar

class RecipeSolver(val solver: Solver) {

    private val log: Logger = LoggerFactory.getLogger(RecipeSolver::class.java)
    fun solve(requirement: RecipeRequirement): RecipeResult {
        solver.apply {
            setTimeLimit(requirement.timeout)
            // 物料数量
            val materials = requirement.materials
            val numRawMaterials = materials.size
            val numMaxMaterials = requirement.maxUseMaterials
            val targetWeight = requirement.targetWeight

            val s = System.currentTimeMillis()

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
            val waterTarget = rangeIndicators.water!!.value
            // 定义产品干净重
            val minDryWeight = targetWeight * (1 - waterTarget.max)
            val maxDryWeight = targetWeight * (1 - waterTarget.min)
            recipeMaterials.map {
                val material = it.value
                val indicators = material.indicators
                it.value.solutionVar.coeff(1 - (indicators.water!!.value))
            }.toTypedArray()
                    .between(minDryWeight, maxDryWeight)

            // 添加成份约束条件
            // 成份要求 总养分 氮含量 磷含量 水溶磷率 钾含量 氯离子 水分 硼 锌
            for (indicator in rangeIndicators) {
                val limit = indicator.value
                if (indicator.isWater) {
                    continue
                }
                if (indicator.isRateToOther) {
                    val otherVar = recipeMaterials.map {
                        val material = it.value
                        val coeff = material.indicators[indicator.otherIndex!!]!!.value
                        it.value.solutionVar.coeff(coeff)
                    }.toTypedArray().sum()

                    val itVar = recipeMaterials.map {
                        val material = it.value
                        val coeff = material.indicators[indicator.itIndex!!]!!.value
                        it.value.solutionVar.coeff(coeff)
                    }.toTypedArray().sum()

                    val minRate = indicator.value.min
                    val maxRate = indicator.value.max
                    itVar.ratioInRange(otherVar, minRate, maxRate)
                } else {
                    recipeMaterials.map {
                        val material = it.value
                        val indicators = material.indicators
                        val coeff = indicators[indicator.index]!!.value
                        material.solutionVar.coeff(coeff)
                    }.toTypedArray()
                            .between(targetWeight * limit.min, targetWeight * limit.max)
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
//                whenVar.eq(16.0)
                when (whenCon.operator) {
                    Operator.EQUAL -> {
                        throw UnsupportedOperationException("不支持等于条件约束")
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
                    Operator.GREATER -> thenVar.gtIf(thenCon.value, boolVar)
                    Operator.LESS -> thenVar.ltIf(thenCon.value, boolVar)
                    Operator.GREATER_EQUAL -> thenVar.geIf(thenCon.value, boolVar)
                    Operator.LESS_EQUAL -> thenVar.leIf(thenCon.value, boolVar)
                }
            }

            // 定义目标函数：最小化成本
            val objective = recipeMaterials.values.map {
                it.solutionVar.coeff(it.price * 1.0 / 1000)
            }.toTypedArray().minimize()

            // 求解
            var e = System.currentTimeMillis()

            val recipeResult = RecipeResult(name)
            log.trace("==================================================")
            while ((e - s) / 1000 < requirement.timeout
                    && recipeResult.recipeCount < requirement.maxResult) {
                recipeResult.addSolveCount()
                solve()
                log.trace(
                        "solve times: " + recipeResult.solveCount + " 耗时：" + (e - s) + "ms " + "变量数量："
                                + numVariables() + " 约束数量："
                                + numConstraints())
                if (numVariables() > 2000 || numConstraints() > 2000) {
                    log.error("变量或约束过多，变量数量：" + numVariables() + " 约束数量：" + numConstraints())
                    return recipeResult
                }
                if (isOptimal()) {
                    // 约束
                    val first = recipeResult.recipes.isEmpty()
                    val recipe = Recipe(requirement, objective.value)
                    recipeResult.addRecipe(recipe)
                    val useMaterials: MutableMap<String, RecipeMaterialValue> = HashMap()
                    recipeMaterials.forEach { (t, u) ->
                        val value = u.solutionVar.value
                        if (value != 0.0) {
                            val material = u.toMaterialValue()
                            recipe.addMaterial(material)
                            if (first) {
                                useMaterials[t] = material
                            }
                        }
                    }

                    val recipeCount = recipeResult.recipeCount
                    // 前十个每3元价差一推，后十个每5元价差一推。
                    log.trace("====================solve size: $recipeCount")
                    val cost = objective.value
                    if (first) {
                        // 后续配方原料不变
                        recipeMaterials.forEach { (id, material) ->
                            if (useMaterials.contains(id)) {
                                material.solutionVar.gt(0.0)
                            } else {
                                material.solutionVar.eq(0.0)
                            }
                        }
                        // 养份保持不变 总养份
                        val totalNutrient = recipe.materials.sumOf { m ->
                            m.totalNutrient()
                        }

                        recipeMaterials.map { (_, material) ->
                            val value = material.totalNutrient()
                            material.solutionVar.coeff(value)
                        }.toTypedArray().eq(totalNutrient)
                    }
                    // 添加价格约束，约束下一个解的范围
                    recipeMaterials.map { (_, material) ->
                        val price = material.price
                        material.solutionVar.coeff(price * 1.0 / 1000)
                    }.toTypedArray().ge(cost + if (recipeCount < 10) 3 else 5)
                } else {
                    log.error("Could not find optimal solution:${getResultStatus()}")
                    return recipeResult
                }
                e = System.currentTimeMillis()
            }
            e = System.currentTimeMillis()
            recipeResult.time = e - s
            return recipeResult
        }
    }

}
