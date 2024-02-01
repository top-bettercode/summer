package top.bettercode.summer.tools.recipe.productioncost

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
        val changes: List<CostChangeLogic>
) {

    fun fee(recipe: Recipe): Double {
        //费用增减
        var allChange = 1.0
        changes.forEach { changeLogic ->
            when (changeLogic.type) {
                WATER_OVER -> {
                    change(recipe.materials, changeLogic, recipe.weight)
                }

                OVER -> {
                    change(recipe.materials, changeLogic, null)
                }

                OTHER -> allChange = changeLogic.changeValue
            }
        }
        //能耗费用
        val energyFee = materialItems.sumOf { (it.change + 1.0) * it.price * it.value }
        //人工+折旧费+其他费用
        val otherFee = dictItems.values.sumOf { (it.change + 1.0) * it.price * it.value }
        //税费 =（人工+折旧费+其他费用）*0.09+15
        val taxFee = otherFee * taxRate + taxFloat
        return (energyFee + otherFee + taxFee) * (1.0 + allChange)
    }

    private fun change(materials: List<RecipeMaterialValue>, changeLogic: CostChangeLogic, value: Double?) {
        val useMaterial = materials.find { it.id == changeLogic.materialId }
        if (useMaterial != null) {
            changeLogic.changeItems!!.forEach { item ->
                when (item.type) {
                    MATERIAL -> {//能耗费用
                        //(1+(value-exceedValue)/eachValue*changeValue)
                        val material = materialItems.find { it.id == item.id }
                        if (material != null) {
                            material.change += ((value
                                    ?: useMaterial.weight) - changeLogic.exceedValue!!) / changeLogic.eachValue!! * changeLogic.changeValue
                        }
                    }

                    DICT -> {
                        when (val dictType = DictType.valueOf(item.id)) {
                            ENERGY -> {
                                materialItems.forEach {
                                    it.change += ((value
                                            ?: useMaterial.weight) - changeLogic.exceedValue!!) / changeLogic.eachValue!! * changeLogic.changeValue
                                }
                            }

                            else -> {
                                val cost = dictItems[dictType]
                                if (cost != null) {
                                    cost.change += ((value
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