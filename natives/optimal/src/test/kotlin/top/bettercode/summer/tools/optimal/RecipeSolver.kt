package top.bettercode.summer.tools.optimal

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanUtils
import top.bettercode.summer.tools.optimal.entity.*
import top.bettercode.summer.tools.optimal.result.Recipe
import top.bettercode.summer.tools.optimal.result.RecipeResult
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale
import top.bettercode.summer.tools.optimal.solver.Solver
import top.bettercode.summer.tools.optimal.solver.`var`.IVar

class RecipeSolver(val solver: Solver) {

    private val log: Logger = LoggerFactory.getLogger(RecipeSolver::class.java)
    fun solve(reqData: ReqData): RecipeResult {
        solver.apply {
            setTimeLimit(reqData.timeout)
            // 物料数量
            val materialsMap = reqData.materials
            val numRawMaterials = materialsMap.size
            val numMaxMaterials = reqData.numMaxMaterials
            val targetWeight = reqData.targetWeight
            val s = System.currentTimeMillis()
            val materialVars = numVarArray(numRawMaterials, reqData.minMaterialWeight, targetWeight)
            val materialVarsMap: MutableMap<String, IVar> = LinkedHashMap()
            var v = 0
            val materialNames = materialsMap.keys.toTypedArray<String>()
            for (materialName in materialNames) {
                materialVarsMap[materialName] = materialVars[v++]
            }

            // 定义产品净重 >=1000kg，含水
            materialVars.between(targetWeight, if (reqData.isAllowDrying) Double.POSITIVE_INFINITY else targetWeight)

            val componentTarget = reqData.componentTarget
            // 水分
            val waterTarget = componentTarget.water
            // 定义产品干净重
            val minDryWeight = targetWeight * (1 - waterTarget.max!!.toDouble())
            val maxDryWeight = targetWeight * (1 - waterTarget.min!!.toDouble())
            materialVarsMap.map {
                val material = materialsMap[it.key]!!
                val components = material.components!!
                it.value.coeff(1 - components.water.value!!.toDouble())
            }.toTypedArray().between(minDryWeight, maxDryWeight)

            val materialReq = reqData.materialReq
            val reqNotZeroKeys = materialReq!!.keys
                    .filter { m: String ->
                        val min = materialReq[m]!!.min
                        min != null && min > 0
                    }
            // 添加成份约束条件
            // 成份要求 总养分 氮含量 磷含量 水溶磷率 钾含量 氯离子 水分 硼 锌
            for (index in componentTarget.keys) {
                val limit = componentTarget[index]!!
                // 不约束水溶磷率、水分
                val limitMaterials = limit.materials
                if ((Components.isWaterSolublePhosphorusRate(index) || Components.isWater(index)) && limitMaterials == null) {
                    continue
                }

                materialVarsMap.map {
                    val materialName = it.key
                    val material = materialsMap[materialName]!!
                    val components = material.components
                    val coeff = components!![index]!!.value!!.toDouble()
                    if (limitMaterials == null || limitMaterials.contains(materialName)
                            || reqNotZeroKeys.any { s: String -> materialName.contains(s) }) {
                        it.value.coeff(coeff)
                    } else {
                        it.value.coeff(0.0)
                    }
                }.toTypedArray().between(targetWeight * limit.min!!.toDouble(), targetWeight * limit.max!!.toDouble())
            }

            // 不能混用的原料
            val notMixMaterials = reqData.notMixMaterials

            // 不选取不能同时使用的原料对
            for (noMixedMaterials in notMixMaterials!!) {
                val noMixedMaterialNames: MutableList<List<String>> = ArrayList()
                for (noMixedMaterial in noMixedMaterials) {
                    val ms: MutableList<String> = ArrayList()
                    for (materialName in materialNames) {
                        if (materialName.contains(noMixedMaterial)) {
                            ms.add(materialName)
                        }
                    }
                    if (ms.isNotEmpty()) {
                        noMixedMaterialNames.add(ms)
                    }
                }
                val length = noMixedMaterials.size
                if (noMixedMaterialNames.size < length) {
                    continue
                }

                //一组变量至多有一个变量可取非零值
//                val noMixedVars = noMixedMaterialNames
//                        .map { names: List<String> ->
//                            val vars = names.map { key: String -> materialVarsMap[key]!! }.toTypedArray()
//                            vars.sum()
//                        }.toTypedArray()
//                noMixedVars.atMostOne()
            }

            // 原料用量
            for (materialNameFragment in materialReq.keys) {
                val limit = materialReq[materialNameFragment]
                if (limit!!.min != null) {
                    val iVars = materialVarsMap.map { (materialName: String, mpVariable: IVar) ->
                        if (materialName.contains(materialNameFragment)) {
                            mpVariable
                        } else {
                            null
                        }
                    }.filterNotNull().toTypedArray()
                    iVars.between(limit.min!!.toDouble(), limit.max!!.toDouble())
                }
            }

            // 进料口数量
            if (numMaxMaterials < numRawMaterials) {
                materialVars.atMost(numMaxMaterials)
            }

            // 水溶磷率限制
            //磷
            val phosphorusVal = materialVarsMap.map {
                val material = materialsMap[it.key]!!
                it.value.coeff(material.components!!.phosphorus!!.value!!.toDouble())
            }.toTypedArray().sum()
            //水溶磷
            val waterSolublePhosphorusVal = materialVarsMap.map {
                val material = materialsMap[it.key]!!
                it.value.coeff(material.components!!.waterSolublePhosphorus!!.value!!.toDouble())
            }.toTypedArray().sum()

            val waterSolublePhosphorusRate = componentTarget.waterSolublePhosphorusRate
            val minRate = waterSolublePhosphorusRate!!.min!!.toDouble()
            val maxRate = waterSolublePhosphorusRate.max!!.toDouble()
            waterSolublePhosphorusVal.ratioInRange(phosphorusVal, minRate, maxRate)

            // 原料比率约束
            val materialRelations = reqData.materialRelations
            // 硫酸
            val vitriolMaterial = materialsMap[ReqData.VITRIOL]
            val normalVal = numVar(0.0, targetWeight)
            val excessVal = numVar(0.0, targetWeight)
            var vitriolVal: IVar? = null
            if (vitriolMaterial != null && reqData.isLimitVitriol) {
                vitriolVal = materialVarsMap[ReqData.VITRIOL]!!
                arrayOf(normalVal, excessVal).eq(vitriolVal)

                val normalMinVars = mutableListOf<IVar>()
                val normalMaxVars = mutableListOf<IVar>()
                val excessMinVars = mutableListOf<IVar>()
                val excessMaxVars = mutableListOf<IVar>()
                val vitriolMaterialRatioMap = materialRelations!![ReqData.VITRIOL]
                for (materialNameFragment in vitriolMaterialRatioMap!!.keys) {
                    materialVarsMap.forEach { (materialName: String, mpVariable: IVar) ->
                        if (materialName.contains(materialNameFragment)) {
                            val materialRatio = vitriolMaterialRatioMap[materialNameFragment]!!
                            val normal = materialRatio.normal!!
                            normalMinVars.add(mpVariable.coeff(normal.min!!.toDouble()))
                            normalMaxVars.add(mpVariable.coeff(normal.max!!.toDouble()))
                            val excess = materialRatio.excess!!
                            excessMinVars.add(mpVariable.coeff(excess.min!!.toDouble()))
                            excessMaxVars.add(mpVariable.coeff(excess.max!!.toDouble()))
                        }
                    }
                }
                normalVal.between(normalMinVars.toTypedArray().sum(), normalMaxVars.toTypedArray().sum())
                excessVal.between(excessMinVars.toTypedArray().sum(), excessMaxVars.toTypedArray().sum())
            }
            // 液氨/碳铵
            // 碳铵用量与液氨用量的换算比例为1公斤液铵等同于4.7647公斤液氨
            // 液氨用量
            var minLiquidAmmoniaVal: IVar? = null
            var maxLiquidAmmoniaVal: IVar? = null
            var liquidAmmoniaVal: IVar? = null
            if (reqData.isLimitLiquidAmmonia) {
                val liquidAmmoniaMaterialRatioMap = materialRelations!![ReqData.LIQUID_AMMONIA]
                liquidAmmoniaVal = materialVarsMap[ReqData.LIQUID_AMMONIA]!!
                val minLiquidVars = mutableListOf<IVar>()
                val maxLiquidVars = mutableListOf<IVar>()

                // 硫酸液氨用量
                if (vitriolVal != null) {
                    val materialRatio = liquidAmmoniaMaterialRatioMap!![ReqData.VITRIOL]
                    if (reqData.isLimitVitriol) {
                        val normal = materialRatio!!.normal!!
                        minLiquidVars.add(normalVal.coeff(normal.min!!.toDouble()))
                        maxLiquidVars.add(normalVal.coeff(normal.max!!.toDouble()))
                        val originExcess = materialRatio.originExcess!!
                        minLiquidVars.add(excessVal.coeff(originExcess.min!!.toDouble()))
                        maxLiquidVars.add(excessVal.coeff(originExcess.max!!.toDouble()))
                    } else {
                        if (materialRatio != null) {
                            val normal = materialRatio.normal!!
                            minLiquidVars.add(vitriolVal.coeff(normal.min!!.toDouble()))
                            maxLiquidVars.add(vitriolVal.coeff(normal.max!!.toDouble()))
                        }
                    }
                }
                for (materialNameFragment in liquidAmmoniaMaterialRatioMap!!.keys) {
                    if (ReqData.VITRIOL != materialNameFragment) {
                        materialVarsMap.forEach { (materialName: String, mpVariable: IVar) ->
                            val needLiquidAmmon: Boolean = ReqData.isNeedLiquidAmmon(materialNameFragment, materialName)
                            if (needLiquidAmmon) {
                                val materialRatio = liquidAmmoniaMaterialRatioMap[materialNameFragment]!!
                                val normal = materialRatio.normal!!
                                minLiquidVars.add(mpVariable.coeff(normal.min!!.toDouble()))
                                maxLiquidVars.add(mpVariable.coeff(normal.max!!.toDouble()))
                            }
                        }
                    }
                }
                minLiquidAmmoniaVal = minLiquidVars.toTypedArray().sum()
                maxLiquidAmmoniaVal = maxLiquidVars.toTypedArray().sum()
                liquidAmmoniaVal.between(minLiquidAmmoniaVal, maxLiquidAmmoniaVal)
            }

            // 条件限制
            // 仅实现 尿素>15 白泥>=80
            val boolVar = boolVar()
            val conditions = reqData.conditions
            conditions!!.forEach { (condition: Condition, condition2: Condition) ->
                val conditionVarArry = materialVarsMap.keys
                        .map { materialName: String ->
                            if (materialName.contains(condition.materialNameFragment)) {
                                return@map materialVarsMap[materialName]
                            }
                            null
                        }.filterNotNull().toTypedArray()
                val expr = conditionVarArry.sum()
                val conditionVarArry2 = materialVarsMap.keys
                        .map { materialName: String ->
                            if (materialName.contains(condition2.materialNameFragment)) {
                                return@map materialVarsMap[materialName]
                            }
                            null
                        }.filterNotNull().toTypedArray()
                val expr2 = conditionVarArry2.sum()
                val value = condition.value
                val value2 = condition2.value
                expr.gtIf(value, boolVar)
                expr.leIfNot(value, boolVar)
                expr2.geIf(value2, boolVar)
            }

            // 定义目标函数：最小化成本
            val objective = materialVarsMap.map { (materialName: String, variable: IVar) ->
                variable.coeff(materialsMap[materialName]!!.price!! * 1.0 / 1000)
            }.toTypedArray().minimize()

            // 求解
            var e = System.currentTimeMillis()
            val recipeResult = RecipeResult(name, reqData)
            log.trace("==================================================")
            while ((e - s) / 1000 < reqData.timeout
                    && recipeResult.recipeCount < reqData.maxResult) {
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
                    //约束检查
                    //混用检查
                    //进料口检查

                    // 限制条件
                    val first = recipeResult.recipes.isEmpty()
                    val recipe = Recipe()
                    recipeResult.addRecipe(recipe)
                    var weight = 0.0
                    val componentRecipe = Components()
                    recipe.componentRecipe = componentRecipe
                    val useMaterials: MutableList<String?> = ArrayList()
                    for (materialName in materialNames) {
                        val mpVariable = materialVarsMap[materialName]!!
                        val solutionValue = mpVariable.value
                        if (solutionValue != 0.0) {
                            weight += solutionValue
                            val material = Material()
                            BeanUtils.copyProperties(materialsMap[materialName]!!, material)
                            if (materialName == ReqData.CLIQUID_AMMONIA) {
                                recipe.isHascliquidAmmonia = true
                            }
                            material.solutionValue = solutionValue
                            recipe.addMaterial(material)
                            if (first) {
                                useMaterials.add(materialName)
                            }
                            val components = material.components
                            for (index in components!!.keys) {
                                val componentLimit = components[index]!!
                                var value = componentLimit.value!!
                                val limit = componentRecipe.computeIfAbsent(
                                        index
                                ) { _: Int? ->
                                    val init = Limit()
                                    BeanUtils.copyProperties(componentLimit, init)
                                    init.max = 0.0
                                    init
                                }!!
                                if (Components.isWaterSolublePhosphorusRate(index)) {
                                    value *= components.phosphorus!!.value!!
                                }
                                limit.max = (limit.max!! + (value * solutionValue)).scale()
                            }
                        }
                    }
                    recipe.vitriolNormal = normalVal.value
                    recipe.vitriolExcess = excessVal.value
                    recipe.minLiquidAmmoniaWeight = (minLiquidAmmoniaVal?.value
                            ?: 0.0)
                    recipe.maxLiquidAmmoniaWeight = (maxLiquidAmmoniaVal?.value
                            ?: 0.0)
                    if (vitriolVal != null) {
                        recipe.vitriolWeight = vitriolVal.value
                    }
                    if (liquidAmmoniaVal != null) {
                        recipe.liquidAmmoniaWeight = liquidAmmoniaVal.value
                    }
                    recipe.dryWater = (weight - targetWeight).scale()
                    val recipeCount = recipeResult.recipeCount
                    // 前十个每3元价差一推，后十个每5元价差一推。
                    log.trace("====================solve size: $recipeCount")
                    val cost = objective.value
                    recipe.cost = (cost)
                    if (first) {
                        recipe.materials
                        // 后续配方原料不变
                        if (reqData.isLimitResultMaterials) {
                            materialVarsMap.forEach { (materialName: String, mpVariable: IVar) ->
                                if (useMaterials.contains(materialName)) {
                                    mpVariable.gt(0.0)
                                } else {
                                    mpVariable.eq(0.0)
                                }
                            }
                        }
                        // 养份保持不变 总养份
                        if (reqData.isLimitResultNutrient) {
                            val totalNutrient = componentRecipe.totalNutrient!!.max!!.toDouble()

                            materialVarsMap.map { (materialName: String, variable: IVar) ->
                                val material = materialsMap[materialName]!!
                                val value = material.components!!.totalNutrient!!.value
                                variable.coeff(value!!.toDouble())
                            }.toTypedArray().eq(totalNutrient)
                        }
                    }
                    // 添加价格约束，限制下一个解的范围
                    materialVarsMap.map { (materialName: String, variable: IVar) ->
                        val price = materialsMap[materialName]!!.price!!
                        variable.coeff(price * 1.0 / 1000)
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
