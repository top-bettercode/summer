package top.bettercode.summer.tools.recipe

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.util.Assert
import top.bettercode.summer.tools.optimal.solver.Sense
import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.criteria.RecipeRelation
import top.bettercode.summer.tools.recipe.criteria.TermThen
import top.bettercode.summer.tools.recipe.indicator.RecipeMaterialIDIndicators
import top.bettercode.summer.tools.recipe.indicator.RecipeRangeIndicators
import top.bettercode.summer.tools.recipe.material.IRecipeMaterial
import top.bettercode.summer.tools.recipe.material.MaterialCondition
import top.bettercode.summer.tools.recipe.material.RecipeMaterial
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs.Companion.toMaterialIDs
import top.bettercode.summer.tools.recipe.material.id.RelationMaterialIDs
import top.bettercode.summer.tools.recipe.material.id.ReplacebleMaterialIDs
import top.bettercode.summer.tools.recipe.productioncost.ProductionCost
import java.util.*
import java.util.function.Predicate

/**
 * 配方要求
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
data class RecipeRequirement(
        /** 产品名称  */
        @JsonProperty("productName")
        val productName: String,
        /** 目标重量，单位KG  */
        @JsonProperty("targetWeight")
        val targetWeight: Double = 1000.0,
        /**
         * 收率
         */
        @JsonProperty("yield")
        val yield: Double = 1.0,
        /** 原料进料口最大数，null不限  */
        @JsonProperty("maxIngredientNum")
        val maxUseMaterialNum: Int? = null,
        /**
         * 最大烘干量，单位KG，null 允许烘干全部水份
         */
        @JsonProperty("maxBakeWeight")
        val maxBakeWeight: Double? = null,
        /**
         * 制造费用
         */
        @JsonProperty("productionCost")
        val productionCost: ProductionCost,
        /** 原料  */
        @JsonProperty("materials")
        val materials: List<RecipeMaterial>,
        /**
         * 指定原料约束
         */
        @JsonProperty("materialIDConstraints")
        val materialIDConstraints: List<TermThen<MaterialIDs, MaterialIDs>>,
        /** 指定用原料ID  */
        @JsonProperty("useMaterialConstraints")
        val useMaterialConstraints: MaterialIDs,
        /** 不能用原料ID  */
        @JsonProperty("noUseMaterialConstraints")
        val noUseMaterialConstraints: MaterialIDs,
        /**
         * 指标范围约束,key：指标ID,value:指标值范围
         */
        @JsonProperty("indicatorRangeConstraints")
        val indicatorRangeConstraints: RecipeRangeIndicators,
        /**
         * 指标指定用原料约束,key:指标ID,value:原料ID
         */
        @JsonProperty("indicatorMaterialIDConstraints")
        val indicatorMaterialIDConstraints: RecipeMaterialIDIndicators,

        /** 原料约束,key:原料ID, value: 原料使用范围约束  */
        @JsonProperty("materialRangeConstraints")
        val materialRangeConstraints: List<TermThen<MaterialIDs, DoubleRange>>,

        /** 不能混用的原料,value: 原料ID  */
        @JsonProperty("notMixMaterialConstraints")
        val notMixMaterialConstraints: List<Array<MaterialIDs>>,
        /**
         *关联原料约束
         */
        @JsonProperty("materialRelationConstraints")
        val materialRelationConstraints: List<TermThen<ReplacebleMaterialIDs, List<TermThen<RelationMaterialIDs, RecipeRelation>>>>,

        /** 条件约束，当条件1满足时，条件2必须满足  */
        @JsonProperty("materialConditionConstraints")
        val materialConditionConstraints: List<TermThen<MaterialCondition, MaterialCondition>>
) {


    /** 超时时间  */
    var timeout = 30

    init {
        Assert.isTrue(targetWeight > 0, "targetWeight must be greater than 0")
        Assert.isTrue(maxUseMaterialNum == null || maxUseMaterialNum > 0, "maxUseMaterialNum must be greater than 0")

        Assert.isTrue(maxBakeWeight == null || maxBakeWeight > 0, "maxBakeWeight must be greater than 0")
    }

    //--------------------------------------------

    companion object {

        /**
         * 生产配方
         * @param productName 产品名称
         * @param targetWeight 目标重量，单位KG
         * @param yield 收率
         * @param maxUseMaterialNum 原料进料口最大数，null不限
         * @param maxBakeWeight 最大烘干量，单位KG，null允许烘干全部水份
         * @param materials 原料
         * @param productionCost 制造费用
         * @param indicatorRangeConstraints 指标范围约束,key：指标ID,value:指标值范围
         * @param indicatorMaterialIDConstraints 指标指定用原料约束,key:指标ID,value:原料ID
         * @param useMaterialConstraints 指定用原料ID
         * @param noUseMaterialConstraints 不能用原料ID
         * @param notMixMaterialConstraints 不能混用的原料,value: 原料ID
         * @param materialRangeConstraints 原料约束,key:原料ID, value: 原料使用范围约束
         * @param materialIDConstraints 指定原料约束
         * @param materialRelationConstraints 关联原料约束
         * @param materialConditionConstraints 条件约束，当条件1满足时，条件2必须满足
         * @return 配方要求
         */
        fun of(productName: String,
               targetWeight: Double,
               yield: Double = 1.0,
               maxUseMaterialNum: Int? = null,
               maxBakeWeight: Double? = null,
               materials: List<RecipeMaterial>,
               productionCost: ProductionCost,
               indicatorRangeConstraints: RecipeRangeIndicators,
               indicatorMaterialIDConstraints: RecipeMaterialIDIndicators,
               useMaterialConstraints: MaterialIDs,
               noUseMaterialConstraints: MaterialIDs,
               notMixMaterialConstraints: List<Array<MaterialIDs>>,
               materialRangeConstraints: List<TermThen<MaterialIDs, DoubleRange>>,
               materialIDConstraints: List<TermThen<MaterialIDs, MaterialIDs>>,
               materialRelationConstraints: List<TermThen<ReplacebleMaterialIDs, List<TermThen<RelationMaterialIDs, RecipeRelation>>>>,
               materialConditionConstraints: List<TermThen<MaterialCondition, MaterialCondition>>
        ): RecipeRequirement {
            val tmpMaterial = materials.associateBy { it.id }
            //约束原料
            indicatorMaterialIDConstraints.values.forEach { indicator ->
                indicator.value = indicator.value.minFrom(tmpMaterial)
            }

            materialRangeConstraints.forEach {
                it.term = it.term.minFrom(tmpMaterial)
            }
            materialRelationConstraints.forEach {
                it.term = it.term.minFrom(tmpMaterial)
                it.then.forEach { then ->
                    then.term = then.term.minFrom(tmpMaterial)
                }
            }

            materialConditionConstraints.forEach { (first, second) ->
                first.materials = first.materials.minFrom(tmpMaterial)
                second.materials = second.materials.minFrom(tmpMaterial)
            }
            // conditoin 转noMix
            val noMixConditions = materialConditionConstraints.filter {
                val op = it.term.condition.sense
                val value = it.term.condition.value
                val otherOp = it.then.condition.sense
                val otherValue = it.then.condition.value
                op == Sense.GT && value == 0.0 && (otherOp == Sense.LE || otherOp == Sense.EQ) && otherValue == 0.0
            }
            val noMixMaterials = noMixConditions.map {
                arrayOf(it.term.materials, it.then.materials)
            }
            val fixNotMixMaterialConstraints = notMixMaterialConstraints + noMixMaterials
            val fixMaterialConditionConstraints = materialConditionConstraints - noMixConditions.toSet()

            // 必选原料
            val materialMust = Predicate { material: IRecipeMaterial ->
                val materialId = material.id

                // 用量>0的原料
                materialRangeConstraints.forEach { (t, u) ->
                    if (t.contains(materialId) && u.min > 0) {
                        return@Predicate true
                    }
                }

                //关联原料
                for (materialRelationConstraint in materialRelationConstraints) {
                    if (materialRelationConstraint.term.contains(materialId)) {
                        return@Predicate true
                    }
                }

                //条件约束
                fixMaterialConditionConstraints.forEach { (_, thenCon) ->
                    if (thenCon.materials.contains(materialId)) {
                        return@Predicate true
                    }
                }

                return@Predicate false
            }
            //不用原料
            val materialFalse = Predicate { material: IRecipeMaterial ->
                val materialId = material.id

                if (materialMust.test(material)) {
                    return@Predicate true
                }

                // 条件 约束原料
                for (condition in fixMaterialConditionConstraints) {
                    if (condition.then.materials.contains(materialId)) {
                        return@Predicate true
                    }
                }

                // 过滤不使用的原料
                if (noUseMaterialConstraints.contains(materialId)) {
                    return@Predicate false
                }

                // 排除全局非限用原料
                if (useMaterialConstraints.ids.isNotEmpty()) {
                    if (!useMaterialConstraints.contains(materialId)) return@Predicate false
                }

                // 排除非限用原料
                materialIDConstraints.forEach { (t, u) ->
                    if (t.contains(materialId) && !u.contains(materialId)) {
                        return@Predicate false
                    }
                }

                // 过滤不在成份约束的原料
                indicatorMaterialIDConstraints.values.forEach { indicator ->
                    val materialIDs = indicator.value
                    val materialIndicator = material.indicators.valueOf(indicator.id)
                    if (materialIndicator > 0 && !materialIDs.contains(materialId)) {
                        return@Predicate false
                    }
                }

                true
            }


            val materialList = materials.groupBy { it.indicators.key }.values
                    .asSequence()
                    .map { list ->
                        val must = list.filter { f -> materialMust.test(f) }
                        val min = list.filter { f -> materialFalse.test(f) }
                                .minOfWithOrNull(materialComparator) { it }
                        if (min == null)
                            must
                        else
                            must + min
                    }.filter { it.isNotEmpty() }.flatten()
                    .toList()

            return RecipeRequirement(productName = productName,
                    targetWeight = targetWeight,
                    yield = `yield`,
                    maxUseMaterialNum = maxUseMaterialNum,
                    maxBakeWeight = maxBakeWeight,
                    productionCost = productionCost,
                    materials = materialList,
                    materialIDConstraints = materialIDConstraints,
                    useMaterialConstraints = useMaterialConstraints,
                    noUseMaterialConstraints = noUseMaterialConstraints,
                    indicatorRangeConstraints = indicatorRangeConstraints,
                    indicatorMaterialIDConstraints = indicatorMaterialIDConstraints,
                    materialRangeConstraints = materialRangeConstraints,
                    notMixMaterialConstraints = fixNotMixMaterialConstraints,
                    materialRelationConstraints = materialRelationConstraints,
                    materialConditionConstraints = fixMaterialConditionConstraints)
        }

        private fun MaterialIDs.minFrom(materials: Map<String, RecipeMaterial>): MaterialIDs {
            val ids = this.mapNotNull { materials[it] }.groupBy { it.indicators.key }.values.mapNotNull { list ->
                list.minOfWithOrNull(materialComparator) { it }?.id
            }
            return MaterialIDs(ids)
        }

        private fun RelationMaterialIDs.minFrom(materials: Map<String, RecipeMaterial>): RelationMaterialIDs {
            val ids = this.mapNotNull { materials[it] }.groupBy { it.indicators.key }.values.mapNotNull { list ->
                list.minOfWithOrNull(materialComparator) { it }?.id
            }
            val relationIds = this.relationIds?.mapNotNull { materials[it] }?.groupBy { it.indicators.key }?.values?.mapNotNull { list ->
                list.minOfWithOrNull(materialComparator) { it }?.id
            }?.toMaterialIDs()
            return RelationMaterialIDs(ids, relationIds)
        }

        private fun ReplacebleMaterialIDs.minFrom(materials: Map<String, RecipeMaterial>): ReplacebleMaterialIDs {
            val ids = this.mapNotNull { materials[it] }.groupBy { it.indicators.key }.values.mapNotNull { list ->
                list.minOfWithOrNull(materialComparator) { it }?.id
            }
            val replaceIds = this.replaceIds?.mapNotNull { materials[it] }?.groupBy { it.indicators.key }?.values?.mapNotNull { list ->
                list.minOfWithOrNull(materialComparator) { it }?.id
            }?.toMaterialIDs()
            return ReplacebleMaterialIDs(ids, this.replaceRate, replaceIds)
        }

        private val materialComparator: Comparator<RecipeMaterial> = Comparator { o1, o2 ->
            if (o1.price == o2.price)
                o1.index.compareTo(o2.index)
            else
                o1.price.compareTo(o2.price)
        }
    }

}