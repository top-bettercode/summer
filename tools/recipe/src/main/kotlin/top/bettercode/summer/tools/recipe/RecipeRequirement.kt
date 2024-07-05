package top.bettercode.summer.tools.recipe

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.optimal.Operator
import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.criteria.RecipeRelation
import top.bettercode.summer.tools.recipe.criteria.TermThen
import top.bettercode.summer.tools.recipe.indicator.RecipeIndicator
import top.bettercode.summer.tools.recipe.indicator.RecipeIndicators
import top.bettercode.summer.tools.recipe.indicator.RecipeMaterialIDIndicators
import top.bettercode.summer.tools.recipe.indicator.RecipeRangeIndicators
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
     *指标
     */
    @JsonProperty("indicators")
    val indicators: List<RecipeIndicator>,
    /**
     * 包装耗材
     */
    @JsonProperty("packagingMaterials")
    val packagingMaterials: List<RecipeOtherMaterial>,
    /** 原料  */
    @JsonProperty("materials")
    var materials: List<RecipeMaterial>,
    /** 保留用原料ID  */
    @JsonProperty("keepMaterialConstraints")
    var keepMaterialConstraints: MaterialIDs,
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
    var materialConditionConstraints: List<TermThen<MaterialCondition, MaterialCondition>>,
    /**
     *关联原料约束,term:消耗的原料，then:关联的原料（trem 原料(relation 消耗此原料的原料)，then:关联的指标）
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
    val indicatorMaterialIDConstraints: RecipeMaterialIDIndicators = RecipeMaterialIDIndicators.EMPTY,

    /** 不能混用的原料,value: 原料ID  */
    @JsonProperty("notMixMaterialConstraints")
    var notMixMaterialConstraints: List<Array<MaterialIDs>>
) {

    @JvmOverloads
    constructor(
        id: String,
        productName: String = id,
        targetWeight: Double,
        yield: Double = 1.0,
        maxUseMaterialNum: Int? = null,
        maxBakeWeight: Double? = null,
        productionCost: ProductionCost,
        indicators: List<RecipeIndicator>,
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
    ) : this(
        id = id,
        productName = productName,
        targetWeight = targetWeight,
        yield = `yield`,
        maxUseMaterialNum = maxUseMaterialNum,
        maxBakeWeight = maxBakeWeight,
        productionCost = productionCost,
        indicators = indicators,
        packagingMaterials = packagingMaterials,
        materials = materials,
        keepMaterialConstraints = keepMaterialConstraints,
        noUseMaterialConstraints = noUseMaterialConstraints,
        indicatorRangeConstraints = indicatorRangeConstraints,
        materialRangeConstraints = materialRangeConstraints,
        materialConditionConstraints = materialConditionConstraints,
        materialRelationConstraints = materialRelationConstraints,
        materialIDConstraints = materialIDConstraints,
        indicatorMaterialIDConstraints = indicatorMaterialIDConstraints,
        notMixMaterialConstraints = emptyList()
    )

    /** 超时时间,单位秒  */
    var timeout: Long = 30L

    @get:JsonIgnore
    val indicatorMap: RecipeIndicators by lazy {
        RecipeIndicators(indicators)
    }

    @get:JsonIgnore
    val materialMust: Predicate<String> by lazy {
        Predicate { materialId: String ->
            // 保留用原料ID
            if (keepMaterialConstraints.contains(materialId)) return@Predicate true

            // 用量>0的原料
            materialRangeConstraints.forEach { (t, u) ->
                if (t.contains(materialId) && ((Operator.GE == u.minOperator && u.min > 0) || (Operator.GT == u.minOperator && u.min >= 0))) {
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
            materialConditionConstraints.forEach { (_, thenCon) ->
                if (thenCon.materials.contains(materialId)) {
                    return@Predicate true
                }
            }

            return@Predicate false
        }
    }

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

        indicatorMaterialIDConstraints.init(indicatorMap)
        indicatorRangeConstraints.init(indicatorMap)
        materials.forEach {
            it.indicators.init(indicatorMap)
        }

        val tmpMaterial = materials.associateBy { it.id }
        //约束原料
        indicatorMaterialIDConstraints.values.forEach { indicator ->
            indicator.value = indicator.value.minFrom(tmpMaterial, true, "指标指定用原料")
        }

        materialRangeConstraints.forEach {
            val u = it.then
            it.term = it.term.minFrom(
                tmpMaterial,
                ((Operator.GE == u.minOperator && u.min > 0) || (Operator.GT == u.minOperator && u.min >= 0)),
                "用量范围约束原料"
            )
        }
        materialRelationConstraints.forEach {
            it.term = it.term.minFrom(tmpMaterial, true, "关联约束消耗原料")
            it.then.forEach { then ->
                then.term = then.term.minFrom(tmpMaterial)
            }
            it.then = it.then.filter { t -> t.term.ids.isNotEmpty() }
        }

        materialConditionConstraints.forEach { (first, second) ->
            first.materials = first.materials.minFrom(tmpMaterial)
            second.materials = second.materials.minFrom(tmpMaterial, true, "条件约束使用原料")
        }
        // conditoin 转noMix
        val noMixConditions = materialConditionConstraints.filter {
            val op = it.term.condition.operator
            val value = it.term.condition.value
            val otherOp = it.then.condition.operator
            val otherValue = it.then.condition.value
            op == Operator.GT && value == 0.0 && (otherOp == Operator.LT || otherOp == Operator.LE || otherOp == Operator.EQ) && otherValue == 0.0
        }
        val noMixMaterials = noMixConditions.map {
            arrayOf(it.term.materials, it.then.materials)
        }
        notMixMaterialConstraints = notMixMaterialConstraints + noMixMaterials
        materialConditionConstraints =
            (materialConditionConstraints - noMixConditions.toSet()).filter { it.term.materials.ids.isNotEmpty() && it.then.materials.ids.isNotEmpty() }

        // 可选原料
        val keepMaterialIds = keepMaterialConstraints.ids.toMutableList()
        //不用原料
        val materialFalse = Predicate { material: IRecipeMaterial ->
            val materialId = material.id
            if (materialMust.test(materialId)) {
                keepMaterialIds.add(materialId)
                return@Predicate true
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
                val materialIndicator = material.indicators[indicator.id]?.scaledValue ?: 0.0
                if (materialIndicator > 0 && !materialIDs.contains(materialId)) {
                    return@Predicate false
                }
            }

            true
        }


        this.materials = materials.groupBy { it.indicators.key }.values
            .asSequence()
            .map { list ->
                val must = list.filter { f -> materialMust.test(f.id) }
                val min = list.filter { f -> materialFalse.test(f) }
                    .minOfWithOrNull(materialComparator) { it }
                if (min == null)
                    must
                else
                    must + min
            }.filter { it.isNotEmpty() }.flatten().distinct()
            .toList()

        this.keepMaterialConstraints =
            if (keepMaterialConstraints.ids.isNotEmpty()) keepMaterialIds.distinct()
                .toMaterialIDs() else keepMaterialConstraints
    }

    //--------------------------------------------
    @JvmOverloads
    fun write(file: File, format: Boolean = false) {
        val objectMapper =
            StringUtil.objectMapper(format = format, include = JsonInclude.Include.NON_NULL)
        objectMapper.writeValue(file, this)
    }

    fun toString(format: Boolean): String {
        val objectMapper =
            StringUtil.objectMapper(format = format, include = JsonInclude.Include.NON_NULL)
        return objectMapper.writeValueAsString(this)
    }

    override fun toString(): String {
        return toString(format = true)
    }

    //--------------------------------------------

    companion object {

        @JvmStatic
        fun read(file: File): RecipeRequirement {
            val objectMapper =
                StringUtil.objectMapper(include = JsonInclude.Include.NON_NULL)
            return objectMapper.readValue(file, RecipeRequirement::class.java)
        }

        @JvmStatic
        fun read(content: String): RecipeRequirement {
            val objectMapper =
                StringUtil.objectMapper(include = JsonInclude.Include.NON_NULL)
            return objectMapper.readValue(content, RecipeRequirement::class.java)
        }


        private fun MaterialIDs.minFrom(
            materials: Map<String, RecipeMaterial>,
            checkExist: Boolean = true,
            msg: String = ""
        ): MaterialIDs {
            val ids =
                this.mapNotNull {
                    materials[it]
                }.groupBy { it.indicators.key }.values.mapNotNull { list ->
                    list.minOfWithOrNull(materialComparator) { it }?.id
                }
            if (checkExist && this.ids.isNotEmpty() && ids.isEmpty()) {
                throw IllegalArgumentException("$msg[$this]不在原料列表中")
            }
            return MaterialIDs(ids)
        }

        private fun RelationMaterialIDs.minFrom(
            materials: Map<String, RecipeMaterial>
        ): RelationMaterialIDs {
            val ids = this.mapNotNull { materials[it] }
                .groupBy { it.indicators.key }.values.mapNotNull { list ->
                    list.minOfWithOrNull(
                        materialComparator
                    ) { it }?.id
                }
            val relationIds = this.relationIds?.mapNotNull { materials[it] }
                ?.groupBy { it.indicators.key }?.values?.mapNotNull { list ->
                    list.minOfWithOrNull(
                        materialComparator
                    ) { it }?.id
                }?.toMaterialIDs()
            return RelationMaterialIDs(ids, relationIds)
        }

        private fun ReplacebleMaterialIDs.minFrom(
            materials: Map<String, RecipeMaterial>,
            checkExist: Boolean, msg: String
        ): ReplacebleMaterialIDs {
            val ids = this.mapNotNull { materials[it] }
                .groupBy { it.indicators.key }.values.mapNotNull { list ->
                    list.minOfWithOrNull(
                        materialComparator
                    ) { it }?.id
                }
            if (checkExist && this.ids.isNotEmpty() && ids.isEmpty()) {
                throw IllegalArgumentException("$msg[${this.ids}]不在原料列表中")
            }
            val replaceIds = this.replaceIds?.mapNotNull {
                materials[it]
            }?.groupBy { it.indicators.key }?.values?.mapNotNull { list ->
                list.minOfWithOrNull(materialComparator) { it }?.id
            }
            if (checkExist && !this.replaceIds?.ids.isNullOrEmpty() && replaceIds.isNullOrEmpty()) {
                throw IllegalArgumentException("$msg[${this.replaceIds}]不在原料列表中")
            }
            return ReplacebleMaterialIDs(ids, this.replaceRate, replaceIds?.toMaterialIDs())
        }

        private val materialComparator: Comparator<RecipeMaterial> = Comparator { o1, o2 ->
            if (o1.price == o2.price)
                o1.index.compareTo(o2.index)
            else
                o1.price.compareTo(o2.price)
        }
    }

}