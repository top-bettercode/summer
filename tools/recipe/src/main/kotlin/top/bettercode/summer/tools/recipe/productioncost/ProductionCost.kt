package top.bettercode.summer.tools.recipe.productioncost

import top.bettercode.summer.tools.recipe.CarrierValue
import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial
import top.bettercode.summer.tools.recipe.productioncost.ChangeItemType.DICT
import top.bettercode.summer.tools.recipe.productioncost.ChangeItemType.MATERIAL
import top.bettercode.summer.tools.recipe.productioncost.ChangeLogicType.*
import top.bettercode.summer.tools.recipe.productioncost.DictType.ENERGY
import top.bettercode.summer.tools.recipe.result.Recipe

/**
 * 制造费用
 *
 * 制造费用合计=人工费+能耗费+折旧费+其他费用+税费
 * 税费=（人工+折旧费+其他费用）*0.09+15
 * 能耗费=煤/生物质耗费+电耗+蒸汽耗+天然气耗+∑其他1~4（若有）
 * 原型增加税率和15
 *
 * @author Peter Wu
 */
data class ProductionCost(
        /**
         * 能耗费用
         */
        val materialItems: List<RecipeOtherMaterial>,
        /**
         * 其他固定费用
         */
        val dictItems: Map<DictType, Cost>,
        /**
         * 税费税率
         */
        val taxRate: Double,
        /**
         * 税费浮动值
         */
        val taxFloat: Double,
        /**
         * 费用增减
         */
        val changes: List<CostChangeLogic>,
        /**
         * 最大增减值
         */
        val maxChange: Double=1000.0
) {

    fun computeFee(recipe: Recipe): ProductionCostValue {
        var allChange = 1.0
        val materialItems = materialItems.map { CarrierValue(it, 1.0) }
        val dictItems = dictItems.mapValues { CarrierValue(it.value, 1.0) }

        //费用增减
        changes.forEach { changeLogic ->
            when (changeLogic.type) {
                WATER_OVER -> {
                    changeProductionCost(recipe.materials, changeLogic, recipe.waterWeight, materialItems, dictItems)
                }

                OVER -> {
                    changeProductionCost(recipe.materials, changeLogic, null, materialItems, dictItems)
                }

                OTHER -> allChange += changeLogic.changeValue
            }
        }
        //人工+折旧费+其他费用
        val otherFee = dictItems.values.sumOf { it.value * it.it.price * it.it.value }

        //能耗费用
        val energyFee = materialItems.sumOf { it.value * it.it.price * it.it.value }

        //税费 =（人工+折旧费+其他费用）*0.09+15
        val taxFee = otherFee * taxRate + taxFloat

        // 制造费用合计=人工费+折旧费+其他费用+能耗费+税费
        val totalFee: Double = (otherFee + energyFee + taxFee) * allChange
        return ProductionCostValue(materialItems, dictItems, otherFee, energyFee, taxFee, totalFee, allChange)
    }

    fun computeFee(materialItems: List<CarrierValue<RecipeOtherMaterial, Double>>?, dictItems: Map<DictType, CarrierValue<Cost, Double>>?): ProductionCostValue? {
        if (materialItems == null || dictItems == null) {
            return null
        }
        var allChange = 1.0
        //费用增减
        changes.forEach { changeLogic ->
            when (changeLogic.type) {
                OTHER -> allChange += changeLogic.changeValue
                else -> {}
            }
        }
        //人工+折旧费+其他费用
        val otherFee = dictItems.values.sumOf { it.value * it.it.price * it.it.value }

        //能耗费用
        val energyFee = materialItems.sumOf { it.value * it.it.price * it.it.value }

        //税费 =（人工+折旧费+其他费用）*0.09+15
        val taxFee = otherFee * taxRate + taxFloat

        // 制造费用合计=人工费+折旧费+其他费用+能耗费+税费
        val totalFee: Double = (otherFee + energyFee + taxFee) * allChange
        return ProductionCostValue(materialItems, dictItems, otherFee, energyFee, taxFee, totalFee, allChange)
    }

    private fun changeProductionCost(materials: List<RecipeMaterialValue>, changeLogic: CostChangeLogic, value: Double?, materialItems: List<CarrierValue<RecipeOtherMaterial, Double>>, dictItems: Map<DictType, CarrierValue<Cost, Double>>) {
        val useMaterial = materials.find { it.id == changeLogic.materialId }
        if (useMaterial != null) {
            changeLogic.changeItems!!.forEach { item ->
                when (item.type) {
                    MATERIAL -> {//能耗费用
                        //(1+(value-exceedValue)/eachValue*changeValue)
                        val material = materialItems.find { it.it.id == item.id }
                        if (material != null) {
                            material.value += ((value
                                    ?: useMaterial.weight) - changeLogic.exceedValue!!) / changeLogic.eachValue!! * changeLogic.changeValue
                        }
                    }

                    DICT -> {
                        when (val dictType = DictType.valueOf(item.id)) {
                            ENERGY -> {
                                materialItems.forEach {
                                    it.value += ((value
                                            ?: useMaterial.weight) - changeLogic.exceedValue!!) / changeLogic.eachValue!! * changeLogic.changeValue
                                }
                            }

                            else -> {
                                val cost = dictItems[dictType]
                                if (cost != null) {
                                    cost.value += ((value
                                            ?: useMaterial.weight) - changeLogic.exceedValue!!) / changeLogic.eachValue!! * changeLogic.changeValue
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}