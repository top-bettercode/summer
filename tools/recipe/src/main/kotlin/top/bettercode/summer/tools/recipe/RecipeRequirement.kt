package top.bettercode.summer.tools.recipe

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.optimal.Sense
import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.criteria.RecipeRelation
import top.bettercode.summer.tools.recipe.criteria.TermThen
import top.bettercode.summer.tools.recipe.indicator.RecipeMaterialIDIndicators
import top.bettercode.summer.tools.recipe.indicator.RecipeRangeIndicators
import top.bettercode.summer.tools.recipe.indicator.RecipeValueIndicators
import top.bettercode.summer.tools.recipe.material.IRecipeMaterial
import top.bettercode.summer.tools.recipe.material.MaterialCondition
import top.bettercode.summer.tools.recipe.material.RecipeMaterial
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs.Companion.toMaterialIDs
import top.bettercode.summer.tools.recipe.material.id.RelationMaterialIDs
import top.bettercode.summer.tools.recipe.material.id.ReplacebleMaterialIDs
import top.bettercode.summer.tools.recipe.productioncost.ProductionCost
import java.io.File
import java.util.function.Predicate

/**
 * 配方要求
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
data class RecipeRequirement(
    /**
     * id
     */
    @JsonProperty("id")
    val id: String,
    /** 产品名称  */
    @JsonProperty("productName")
    val productName: String = id,
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
    /**
     *系统指标
     */
    @JsonProperty("systemIndicators")
    val systemIndicators: RecipeValueIndicators,
    /**
     * 包装耗材
     */
    @JsonProperty("packagingMaterials")
    val packagingMaterials: List<RecipeOtherMaterial>,
    /** 原料  */
    @JsonProperty("materials")
    val materials: List<RecipeMaterial>,

    /** 保留用原料ID  */
    @JsonProperty("keepMaterialConstraints")
    val keepMaterialConstraints: MaterialIDs,
    /** 不能用原料ID  */
    @JsonProperty("noUseMaterialConstraints")
    val noUseMaterialConstraints: MaterialIDs,
    /**
     * 指标范围约束,key：指标ID,value:指标值范围
     */
    @JsonProperty("indicatorRangeConstraints")
    val indicatorRangeConstraints: RecipeRangeIndicators,

    /** 原料约束,key:原料ID, value: 原料使用范围约束  */
    @JsonProperty("materialRangeConstraints")
    val materialRangeConstraints: List<TermThen<MaterialIDs, DoubleRange>>,
    /** 条件约束，当条件1满足时，条件2必须满足  */
    @JsonProperty("materialConditionConstraints")
    val materialConditionConstraints: List<TermThen<MaterialCondition, MaterialCondition>>,
    /**
     *关联原料约束
     */
    @JsonProperty("materialRelationConstraints")
    val materialRelationConstraints: List<TermThen<ReplacebleMaterialIDs, List<TermThen<RelationMaterialIDs, RecipeRelation>>>>,
    /**
     * 指定原料约束
     */
    @JsonProperty("materialIDConstraints")
    val materialIDConstraints: List<TermThen<MaterialIDs, MaterialIDs>> = emptyList(),
    /**
     * 指标指定用原料约束,key:指标ID,value:原料ID
     */
    @JsonProperty("indicatorMaterialIDConstraints")
    val indicatorMaterialIDConstraints: RecipeMaterialIDIndicators = RecipeMaterialIDIndicators(),

    /** 不能混用的原料,value: 原料ID  */
    @JsonProperty("notMixMaterialConstraints")
    val notMixMaterialConstraints: List<Array<MaterialIDs>>
) {


    /** 超时时间,单位秒  */
    var timeout: Long = 30L

    init {
        if (targetWeight <= 0) {
            throw IllegalArgumentException("targetWeight must be greater than 0")
        }
        if (maxUseMaterialNum != null && maxUseMaterialNum <= 0) {
            throw IllegalArgumentException("maxUseMaterialNum must be greater than 0")
        }
        if (maxBakeWeight != null && maxBakeWeight <= 0) {
            throw IllegalArgumentException("maxBakeWeight must be greater than 0")
        }
    }

    //--------------------------------------------
    fun write(file: File) {
        val objectMapper =
            StringUtil.objectMapper(format = true, include = JsonInclude.Include.NON_NULL)
        objectMapper.writeValue(file, this)
    }
    //--------------------------------------------

    companion object {

        @JvmStatic
        fun read(file: File): RecipeRequirement {
            val objectMapper =
                StringUtil.objectMapper(format = true, include = JsonInclude.Include.NON_NULL)
            return objectMapper.readValue(file, RecipeRequirement::class.java)
        }

        /**
         * 生产配方要求
         * @param id id
         * @param productName 产品名称
         * @param targetWeight 目标重量，单位KG
         * @param yield 收率
         * @param maxUseMaterialNum 原料进料口最大数，null不限
         * @param maxBakeWeight 最大烘干量，单位KG，null 允许烘干全部水份
         * @param productionCost 制造费用
         * @param packagingMaterials 包装耗材
         * @param materials 原料
         * @param keepMaterialConstraints 保留用原料ID
         * @param noUseMaterialConstraints 不能用原料ID
         * @param indicatorRangeConstraints 指标范围约束,key：指标ID,value:指标值范围
         * @param materialRangeConstraints 原料约束,key:原料ID, value: 原料使用范围约束
         * @param materialConditionConstraints 条件约束，当条件1满足时，条件2必须满足
         * @param materialRelationConstraints 关联原料约束
         * @param materialIDConstraints 指定原料约束
         * @param indicatorMaterialIDConstraints 指标指定用原料约束,key:指标ID,value:原料ID
         * @param notMixMaterialConstraints 不能混用的原料,value: 原料ID
         * @return 配方要求
         */
        @JvmStatic
        @JvmOverloads
        fun of(
            id: String,
            productName: String = id,
            targetWeight: Double,
            yield: Double = 1.0,
            maxUseMaterialNum: Int? = null,
            maxBakeWeight: Double? = null,
            productionCost: ProductionCost,
            systemIndicators: RecipeValueIndicators,
            packagingMaterials: List<RecipeOtherMaterial>,
            materials: List<RecipeMaterial>,
            keepMaterialConstraints: MaterialIDs,
            noUseMaterialConstraints: MaterialIDs,
            indicatorRangeConstraints: RecipeRangeIndicators,
            materialRangeConstraints: List<TermThen<MaterialIDs, DoubleRange>>,
            materialConditionConstraints: List<TermThen<MaterialCondition, MaterialCondition>>,
            materialRelationConstraints: List<TermThen<ReplacebleMaterialIDs, List<TermThen<RelationMaterialIDs, RecipeRelation>>>>,
            materialIDConstraints: List<TermThen<MaterialIDs, MaterialIDs>> = emptyList(),
            indicatorMaterialIDConstraints: RecipeMaterialIDIndicators = RecipeMaterialIDIndicators(),
            notMixMaterialConstraints: List<Array<MaterialIDs>> = emptyList(),
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
                it.then = it.then.filter { t -> t.term.ids.isNotEmpty() }
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
                op == Sense.GT && value == 0.0 && (otherOp == Sense.LT || otherOp == Sense.LE || otherOp == Sense.EQ) && otherValue == 0.0
            }
            val noMixMaterials = noMixConditions.map {
                arrayOf(it.term.materials, it.then.materials)
            }
            val fixNotMixMaterialConstraints = notMixMaterialConstraints + noMixMaterials
            val fixMaterialConditionConstraints =
                (materialConditionConstraints - noMixConditions.toSet()).filter { it.term.materials.ids.isNotEmpty() && it.then.materials.ids.isNotEmpty() }

            // 可选原料
            val keepMaterialIds = keepMaterialConstraints.ids.toMutableList()
            val materialMust = Predicate { material: IRecipeMaterial ->
                val materialId = material.id

                // 保留用原料ID
                if (keepMaterialIds.isNotEmpty()) {
                    if (keepMaterialConstraints.contains(materialId)) return@Predicate true
                }

                // 用量>0的原料
                materialRangeConstraints.forEach { (t, u) ->
                    if (t.contains(materialId) && ((Sense.GE == u.minSense && u.min > 0) || (Sense.GT == u.minSense && u.min >= 0))) {
                        keepMaterialIds.add(materialId)
                        return@Predicate true
                    }
                }

                //关联原料
                for (materialRelationConstraint in materialRelationConstraints) {
                    if (materialRelationConstraint.term.contains(materialId)) {
                        keepMaterialIds.add(materialId)
                        return@Predicate true
                    }
                }

                //条件约束
                fixMaterialConditionConstraints.forEach { (_, thenCon) ->
                    if (thenCon.materials.contains(materialId)) {
                        keepMaterialIds.add(materialId)
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
                if (keepMaterialConstraints.ids.isNotEmpty()) {
                    if (!keepMaterialConstraints.contains(materialId)) return@Predicate false
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
                }.filter { it.isNotEmpty() }.flatten().distinct()
                .toList()
            return RecipeRequirement(
                id = id,
                productName = productName,
                targetWeight = targetWeight,
                yield = `yield`,
                maxUseMaterialNum = maxUseMaterialNum,
                maxBakeWeight = maxBakeWeight,
                productionCost = productionCost,
                systemIndicators = systemIndicators,
                packagingMaterials = packagingMaterials,
                materials = materialList,
                materialIDConstraints = materialIDConstraints,
                keepMaterialConstraints = if (keepMaterialConstraints.ids.isNotEmpty()) keepMaterialIds.distinct()
                    .toMaterialIDs() else keepMaterialConstraints,
                noUseMaterialConstraints = noUseMaterialConstraints,
                indicatorRangeConstraints = indicatorRangeConstraints,
                indicatorMaterialIDConstraints = indicatorMaterialIDConstraints,
                materialRangeConstraints = materialRangeConstraints.filter { it.term.ids.isNotEmpty() },
                notMixMaterialConstraints = fixNotMixMaterialConstraints,
                materialConditionConstraints = fixMaterialConditionConstraints,
                materialRelationConstraints = materialRelationConstraints.filter { it.term.ids.isNotEmpty() && it.then.isNotEmpty() },
            )
        }

        private fun MaterialIDs.minFrom(materials: Map<String, RecipeMaterial>): MaterialIDs {
            val ids = this.mapNotNull { materials[it] }
                .groupBy { it.indicators.key }.values.mapNotNull { list ->
                    list.minOfWithOrNull(materialComparator) { it }?.id
                }
            return MaterialIDs(ids)
        }

        private fun RelationMaterialIDs.minFrom(materials: Map<String, RecipeMaterial>): RelationMaterialIDs {
            val ids = this.mapNotNull { materials[it] }
                .groupBy { it.indicators.key }.values.mapNotNull { list ->
                    list.minOfWithOrNull(materialComparator) { it }?.id
                }
            val relationIds = this.relationIds?.mapNotNull { materials[it] }
                ?.groupBy { it.indicators.key }?.values?.mapNotNull { list ->
                    list.minOfWithOrNull(materialComparator) { it }?.id
                }?.toMaterialIDs()
            return RelationMaterialIDs(ids, relationIds)
        }

        private fun ReplacebleMaterialIDs.minFrom(materials: Map<String, RecipeMaterial>): ReplacebleMaterialIDs {
            val ids = this.mapNotNull { materials[it] }
                .groupBy { it.indicators.key }.values.mapNotNull { list ->
                    list.minOfWithOrNull(materialComparator) { it }?.id
                }
            val replaceIds = this.replaceIds?.mapNotNull { materials[it] }
                ?.groupBy { it.indicators.key }?.values?.mapNotNull { list ->
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