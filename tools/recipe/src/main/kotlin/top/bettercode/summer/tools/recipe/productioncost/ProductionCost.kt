package top.bettercode.summer.tools.recipe.productioncost

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.recipe.CarrierValue
import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial
import top.bettercode.summer.tools.recipe.productioncost.ChangeItemType.DICT
import top.bettercode.summer.tools.recipe.productioncost.ChangeItemType.MATERIAL
import top.bettercode.summer.tools.recipe.productioncost.ChangeLogicType.*
import top.bettercode.summer.tools.recipe.productioncost.DictType.ENERGY
import top.bettercode.summer.tools.recipe.result.Recipe
import java.util.*

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
@JsonPropertyOrder(alphabetic = true)
data class ProductionCost(
    /**
     * 能耗费用
     */
    @JsonProperty("materialItems")
    val materialItems: List<RecipeOtherMaterial>,
    /**
     * 其他固定费用
     */
    @JsonProperty("dictItems")
    val dictItems: SortedMap<DictType, Cost>,
    /**
     * 税费税率
     */
    @JsonProperty("taxRate")
    val taxRate: Double,
    /**
     * 税费浮动值
     */
    @JsonProperty("taxFloat")
    val taxFloat: Double,
    /**
     * 费用增减
     */
    @JsonProperty("changes")
    val changes: List<CostChangeLogic>,
    /**
     * 费用增减当使用原料时生效
     */
    @JsonProperty("changeWhenMaterialUsed")
    val changeWhenMaterialUsed: Boolean = true,
) {

    fun computeFee(recipe: Recipe): ProductionCostValue {
        return computeFee(
            materials = recipe.materials,
            waterWeight = recipe.waterWeight,
            scale = recipe.scale,
            minEpsilon = recipe.minEpsilon
        )
    }

    fun computeFee(
        materials: List<RecipeMaterialValue>,
        waterWeight: Double,
        scale: Int,
        minEpsilon: Double,
    ): ProductionCostValue {
        var allChange = 1.0

        val toZeros = changes.filter { it.toZero }
        val mIds = toZeros.flatMap {
            it.changeItems!!.filter { it.type == MATERIAL }.map { it.id }
        }
        val dTypes = toZeros.flatMap {
            it.changeItems!!.filter { it.type == DICT }
                .map { DictType.valueOf(it.id) }
        }
        val materialItems =
            materialItems.map {
                CarrierValue(
                    it,
                    if (mIds.contains(it.id)) 0.0 else 1.0
                )
            }
        val dictItems = dictItems.mapValues {
            CarrierValue(
                it.value,
                if (dTypes.contains(it.key)) 0.0 else 1.0
            )
        }

        //费用增减
        changes.filter { !it.toZero }.forEach { changeLogic ->
            when (changeLogic.type) {
                WATER_OVER -> {
                    changeProductionCost(
                        materials,
                        changeLogic,
                        waterWeight,
                        materialItems,
                        dictItems
                    )
                }

                OVER -> {
                    changeProductionCost(
                        materials,
                        changeLogic,
                        null,
                        materialItems,
                        dictItems
                    )
                }

                OTHER -> allChange += changeLogic.changeValue
            }
        }
        //人工+折旧费+其他费用
        val otherFee =
            dictItems.values.sumOf { it.value * it.it.price * it.it.value }

        //能耗费用
        val energyFee =
            materialItems.sumOf { it.value * it.it.price * it.it.value }

        //税费 =（人工+折旧费+其他费用）*0.09+15
        val taxFee = otherFee * taxRate + taxFloat

        // 制造费用合计=人工费+折旧费+其他费用+能耗费+税费
        val totalFee: Double = (otherFee + energyFee + taxFee) * allChange
        return ProductionCostValue(
            materialItems = materialItems,
            dictItems = dictItems,
            otherFee = otherFee,
            energyFee = energyFee,
            taxFee = taxFee,
            totalFee = totalFee,
            allChange = allChange,
            scale = scale,
            minEpsilon = minEpsilon
        )
    }

    fun computeFee(
        materialItems: List<CarrierValue<RecipeOtherMaterial, Double>>?,
        dictItems: Map<DictType, CarrierValue<Cost, Double>>?,
        scale: Int,
        minEpsilon: Double,
    ): ProductionCostValue? {
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
        val otherFee =
            dictItems.values.sumOf { it.value * it.it.price * it.it.value }

        //能耗费用
        val energyFee =
            materialItems.sumOf { it.value * it.it.price * it.it.value }

        //税费 =（人工+折旧费+其他费用）*0.09+15
        val taxFee = otherFee * taxRate + taxFloat

        // 制造费用合计=人工费+折旧费+其他费用+能耗费+税费
        val totalFee: Double = (otherFee + energyFee + taxFee) * allChange
        return ProductionCostValue(
            materialItems = materialItems,
            dictItems = dictItems,
            otherFee = otherFee,
            energyFee = energyFee,
            taxFee = taxFee,
            totalFee = totalFee,
            allChange = allChange,
            scale = scale,
            minEpsilon = minEpsilon
        )
    }

    private fun changeProductionCost(
        materials: List<RecipeMaterialValue>,
        changeLogic: CostChangeLogic,
        value: Double?,
        materialItems: List<CarrierValue<RecipeOtherMaterial, Double>>,
        dictItems: Map<DictType, CarrierValue<Cost, Double>>,
    ) {
        val useMaterial =
            materials.filter { changeLogic.materialId?.contains(it.id) == true }.sumOf { it.weight }
        if (!changeWhenMaterialUsed || useMaterial > 0) {
            val useVar = value ?: useMaterial
            val exceedValue = changeLogic.exceedValue!!
            val changeRate = changeLogic.changeValue / changeLogic.eachValue!!
            changeLogic.changeItems!!.forEach { item ->
                when (item.type) {
                    MATERIAL -> {//能耗费用
                        //(1+(value-exceedValue)/eachValue*changeValue)
                        val material = materialItems.find { it.it.id == item.id }
                        if (material != null) {
                            material.value += (useVar - exceedValue) * changeRate
                        }
                    }

                    DICT -> {
                        when (val dictType = DictType.valueOf(item.id)) {
                            ENERGY -> {
                                materialItems.forEach {
                                    it.value += (useVar - exceedValue) * changeRate
                                }
                            }

                            else -> {
                                val cost = dictItems[dictType]
                                if (cost != null) {
                                    cost.value += (useVar - exceedValue) * changeRate
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}