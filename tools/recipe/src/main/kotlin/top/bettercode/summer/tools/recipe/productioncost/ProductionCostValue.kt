package top.bettercode.summer.tools.recipe.productioncost

import top.bettercode.summer.tools.recipe.CarrierValue
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial

/**
 *
 * @author Peter Wu
 */
class ProductionCostValue(
        /**
         * 能耗费用
         */
        val materialItems: List<CarrierValue<RecipeOtherMaterial, Double>>,
        /**
         * 其他固定费用
         */
        val dictItems: Map<DictType, CarrierValue<Cost, Double>>,
        /**
         * 人工+折旧费+其他费用
         *
         */
        val otherFee: Double,
        /**
         * 能耗费用
         */
        val energyFee: Double,
        /**
         *  税费 =（人工+折旧费+其他费用）*0.09+15
         *
         */
        val taxFee: Double,
        /**
         *  制造费用合计=人工费+折旧费+其他费用+能耗费+税费
         *
         */
        val totalFee: Double
)