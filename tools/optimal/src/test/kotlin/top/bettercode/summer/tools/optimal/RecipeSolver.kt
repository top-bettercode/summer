package top.bettercode.summer.tools.optimal

import top.bettercode.summer.tools.optimal.entity.*
import top.bettercode.summer.tools.optimal.result.Recipe
import top.bettercode.summer.tools.optimal.result.RecipeResult
import top.bettercode.summer.tools.optimal.solver.Solver
import top.bettercode.summer.tools.optimal.solver.`var`.IVar
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanUtils
import java.math.BigDecimal

class RecipeSolver(val solver: Solver) {

    private val log: Logger = LoggerFactory.getLogger(RecipeSolver::class.java)
    fun solve(reqData: ReqData): RecipeResult {
        solver.setTimeLimit(reqData.timeout)
        // 物料数量
        val materialsMap = reqData.materials
        val numRawMaterials = materialsMap.size
        val numMaxMaterials = reqData.numMaxMaterials
        val targetWeight = reqData.targetWeight.toDouble()
        val s = System.currentTimeMillis()
        val materialVars = solver.numVarArray(numRawMaterials, reqData.minMaterialWeight, targetWeight)
        val materialVarsMap: MutableMap<String, IVar> = LinkedHashMap()
        var v = 0
        val materialNames = materialsMap.keys.toTypedArray<String>()
        for (materialName in materialNames) {
            materialVarsMap[materialName] = materialVars[v++]
        }

        // 定义产品净重 >=1000kg，含水
        solver.between(materialVars, targetWeight, if (reqData.isAllowDrying) Double.POSITIVE_INFINITY else targetWeight)

        val componentTarget = reqData.componentTarget
        // 水分
        val waterTarget = componentTarget.water
        // 定义产品干净重
        val minDryWeight = targetWeight * (1 - waterTarget.max!!.toDouble())
        val maxDryWeight = targetWeight * (1 - waterTarget.min!!.toDouble())
        solver.between(materialVarsMap.map {
            val material = materialsMap[it.key]!!
            val components = material.components!!
            it.value.coeff(1 - components.water.value!!.toDouble())
        }.toTypedArray(), minDryWeight, maxDryWeight)

        val materialReq = reqData.materialReq
        val reqNotZeroKeys = materialReq!!.keys
                .filter { m: String ->
                    val min = materialReq[m]!!.min
                    min != null && min > BigDecimal.ZERO
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

            solver.between(materialVarsMap.map {
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
            }.toTypedArray(), targetWeight * limit.min!!.toDouble(), targetWeight * limit.max!!.toDouble())
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
            val noMixedVars = noMixedMaterialNames
                    .map { names: List<String> ->
                        val vars = names.map { key: String -> materialVarsMap[key]!! }.toTypedArray()
                        solver.sum(vars)
                    }.toTypedArray()
            solver.atMostOne(noMixedVars)
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
                solver.between(iVars, limit.min!!.toDouble(), limit.max!!.toDouble())
            }
        }

        // 进料口数量
        if (numMaxMaterials < numRawMaterials) {
            solver.atMost(materialVars, numMaxMaterials)
        }

        // 水溶磷率限制
        //磷
        val phosphorusVal = solver.sum(materialVarsMap.map {
            val material = materialsMap[it.key]!!
            it.value.coeff(material.components!!.phosphorus!!.value!!.toDouble())
        }.toTypedArray())
        //水溶磷
        val waterSolublePhosphorusVal = solver.sum(materialVarsMap.map {
            val material = materialsMap[it.key]!!
            it.value.coeff(material.components!!.waterSolublePhosphorus!!.value!!.toDouble())
        }.toTypedArray())

        val waterSolublePhosphorusRate = componentTarget.waterSolublePhosphorusRate
        val minRate = waterSolublePhosphorusRate!!.min!!.toDouble()
        val maxRate = waterSolublePhosphorusRate.max!!.toDouble()
        solver.between(waterSolublePhosphorusVal, phosphorusVal, minRate, maxRate)

        // 原料比率约束
        val materialRelations = reqData.materialRelations
        // 硫酸
        val vitriolMaterial = materialsMap[ReqData.vitriol]
        val normalVal = solver.numVar(0.0, targetWeight)
        val excessVal = solver.numVar(0.0, targetWeight)
        var vitriolVal: IVar? = null
        if (vitriolMaterial != null && reqData.isLimitVitriol) {
            vitriolVal = materialVarsMap[ReqData.vitriol]!!
            solver.eq(arrayOf(normalVal, excessVal), vitriolVal)

            val normalMinVars = mutableListOf<IVar>()
            val normalMaxVars = mutableListOf<IVar>()
            val excessMinVars = mutableListOf<IVar>()
            val excessMaxVars = mutableListOf<IVar>()
            val vitriolMaterialRatioMap = materialRelations!![ReqData.vitriol]
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
            solver.between(normalVal, solver.sum(normalMinVars.toTypedArray()), solver.sum(normalMaxVars.toTypedArray()))
            solver.between(excessVal, solver.sum(excessMinVars.toTypedArray()), solver.sum(excessMaxVars.toTypedArray()))
        }
        // 液氨/碳铵
        // 碳铵用量与液氨用量的换算比例为1公斤液铵等同于4.7647公斤液氨
        // 液氨用量
        var minLiquidAmmoniaVal: IVar? = null
        var maxLiquidAmmoniaVal: IVar? = null
        var liquidAmmoniaVal: IVar? = null
        if (reqData.isLimitLiquidAmmonia) {
            val liquidAmmoniaMaterialRatioMap = materialRelations!![ReqData.liquidAmmonia]
            liquidAmmoniaVal = materialVarsMap[ReqData.liquidAmmonia]!!
            val minLiquidVars = mutableListOf<IVar>()
            val maxLiquidVars = mutableListOf<IVar>()

            // 硫酸液氨用量
            if (vitriolVal != null) {
                val materialRatio = liquidAmmoniaMaterialRatioMap!![ReqData.vitriol]
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
                if (ReqData.vitriol != materialNameFragment) {
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
            minLiquidAmmoniaVal = solver.sum(minLiquidVars.toTypedArray())
            maxLiquidAmmoniaVal = solver.sum(maxLiquidVars.toTypedArray())
            solver.between(liquidAmmoniaVal, minLiquidAmmoniaVal, maxLiquidAmmoniaVal)
        }

        // 条件限制
        // 仅实现 尿素>15 白泥>=80
        val boolVar = solver.boolVar()
        val conditions = reqData.conditions
        conditions!!.forEach { (condition: Condition, condition2: Condition) ->
            val conditionVarArry = materialVarsMap.keys
                    .map { materialName: String ->
                        if (materialName.contains(condition.materialNameFragment)) {
                            return@map materialVarsMap[materialName]
                        }
                        null
                    }.filterNotNull().toTypedArray()
            val expr = solver.sum(conditionVarArry)
            val conditionVarArry2 = materialVarsMap.keys
                    .map { materialName: String ->
                        if (materialName.contains(condition2.materialNameFragment)) {
                            return@map materialVarsMap[materialName]
                        }
                        null
                    }.filterNotNull().toTypedArray()
            val expr2 = solver.sum(conditionVarArry2)
            val value = condition.value.toDouble()
            val value2 = condition2.value.toDouble()
            solver.geIf(expr, value + solver.epsilon, boolVar)
            solver.leIfNot(expr, value, boolVar)
            solver.geIf(expr2, value2, boolVar)
        }

        // 定义目标函数：最小化成本
        val objective = solver.minimize(materialVarsMap.map { (materialName: String, variable: IVar) ->
            variable.coeff(materialsMap[materialName]!!.price!! * 1.0 / 1000)
        }.toTypedArray())

        // 求解
        var e = System.currentTimeMillis()
        val recipeResult = RecipeResult(solver.name, reqData)
        System.err.println("==================================================")
        while ((e - s) / 1000 < reqData.timeout
                && recipeResult.recipeCount < reqData.maxResult) {
            recipeResult.addSolveCount()
            solver.solve()
            System.err.println(
                    "solve times: " + recipeResult.solveCount + " 耗时：" + (e - s) + "ms " + "变量数量："
                            + solver.numVariables() + " 约束数量："
                            + solver.numConstraints())
            if (solver.numVariables() > 2000 || solver.numConstraints() > 2000) {
                log.error("变量或约束过多，变量数量：" + solver.numVariables() + " 约束数量：" + solver.numConstraints())
                return recipeResult
            }
            if (solver.isOptimal()) {
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
                        if (materialName == ReqData.cliquidAmmonia) {
                            recipe.isHascliquidAmmonia = true
                        }
                        material.solutionValue = BigDecimal.valueOf(solutionValue)
                        recipe.addMaterial(material)
                        if (first) {
                            useMaterials.add(materialName)
                        }
                        val components = material.components
                        for (index in components!!.keys) {
                            val componentLimit = components[index]!!
                            var value = componentLimit.value
                            val limit = componentRecipe.computeIfAbsent(
                                    index
                            ) { _: Int? ->
                                val init = Limit()
                                BeanUtils.copyProperties(componentLimit, init)
                                init.max = BigDecimal.ZERO
                                init
                            }!!
                            if (Components.isWaterSolublePhosphorusRate(index)) {
                                value = components.phosphorus!!.value!!.multiply(value)
                            }
                            limit.max = limit.max!!.add(value!!.multiply(BigDecimal.valueOf(solutionValue)))
                        }
                    }
                }
                recipe.vitriolNormal = BigDecimal.valueOf(normalVal.value)
                recipe.vitriolExcess = BigDecimal.valueOf(excessVal.value)
                recipe.minLiquidAmmoniaWeight = BigDecimal.valueOf(minLiquidAmmoniaVal?.value
                        ?: 0.0)
                recipe.maxLiquidAmmoniaWeight = BigDecimal.valueOf(maxLiquidAmmoniaVal?.value
                        ?: 0.0)
                if (vitriolVal != null) {
                    recipe.vitriolWeight = BigDecimal.valueOf(vitriolVal.value)
                }
                if (liquidAmmoniaVal != null) {
                    recipe.liquidAmmoniaWeight = BigDecimal.valueOf(liquidAmmoniaVal.value)
                }
                recipe.dryWater = BigDecimal.valueOf(weight).subtract(BigDecimal.valueOf(targetWeight))
                val recipeCount = recipeResult.recipeCount
                // 前十个每3元价差一推，后十个每5元价差一推。
                System.err.println("====================solve size: $recipeCount")
                val cost = objective.value
                recipe.cost = BigDecimal.valueOf(cost)
                if (first) {
                    recipe.materials
                    // 后续配方原料不变
                    if (reqData.isLimitResultMaterials) {
                        materialVarsMap.forEach { (materialName: String, mpVariable: IVar) ->
                            if (useMaterials.contains(materialName)) {
                                solver.ge(mpVariable, solver.epsilon)
                            } else {
                                solver.eq(mpVariable, 0.0)
                            }
                        }
                    }
                    // 养份保持不变 总养份
                    if (reqData.isLimitResultNutrient) {
                        val totalNutrient = componentRecipe.totalNutrient!!.max!!.toDouble()

                        solver.eq(materialVarsMap.map { (materialName: String, variable: IVar) ->
                            val material = materialsMap[materialName]!!
                            val value = material.components!!.totalNutrient!!.value
                            variable.coeff(value!!.toDouble())
                        }.toTypedArray(), totalNutrient)
                    }
                }
                // 添加价格约束，限制下一个解的范围
                solver.ge(materialVarsMap.map { (materialName: String, variable: IVar) ->
                    val price = materialsMap[materialName]!!.price!!
                    variable.coeff(price * 1.0 / 1000)
                }.toTypedArray(), cost + if (recipeCount < 10) 3 else 5)
            } else {
                log.error("Could not find optimal solution:${solver.getResultStatus()}")
                return recipeResult
            }
            e = System.currentTimeMillis()
        }
        e = System.currentTimeMillis()
        recipeResult.time = e - s
        return recipeResult
    }

}
