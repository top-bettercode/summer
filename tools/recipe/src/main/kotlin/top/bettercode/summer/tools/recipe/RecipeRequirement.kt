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
    val materials: List<RecipeMaterial>,
    /** 保留用原料ID  */
    @JsonProperty("keepMaterialConstraints")
    var keepMaterialConstraints: MaterialIDs,
    /** 不能用原料ID  */
    @JsonProperty("noUseMaterialConstraints")
    var noUseMaterialConstraints: MaterialIDs,
    /**
     * 指标范围约束,key：指标ID,value:指标值范围
     */
    @JsonProperty("indicatorRangeConstraints")
    val indicatorRangeConstraints: RecipeRangeIndicators,

    /** 原料约束,key:原料ID, value: 原料使用范围约束  */
    @JsonProperty("materialRangeConstraints")
    var materialRangeConstraints: List<TermThen<MaterialIDs, DoubleRange>>,
    /** 条件约束，当条件1满足时，条件2必须满足  */
    @JsonProperty("materialConditionConstraints")
    var materialConditionConstraints: List<TermThen<MaterialCondition, MaterialCondition>>,
    /**
     *关联原料约束,term:消耗的原料，then:关联的原料（trem 原料(relation 消耗此原料的原料)，then:关联的指标）
     */
    @JsonProperty("materialRelationConstraints")
    var materialRelationConstraints: List<TermThen<ReplacebleMaterialIDs, List<TermThen<RelationMaterialIDs, RecipeRelation>>>>,
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
    var notMixMaterialConstraints: List<Array<MaterialIDs>>,
    /**
     * 指标小数位
     */
    @JsonProperty("indicatorScale")
    val indicatorScale: Int = 5,
) {

    /** 超时时间,单位秒  */
    var timeout: Long = 30L

    @get:JsonIgnore
    val indicatorFormat: String = "0.${"0".repeat(indicatorScale - 2)}%"

    @get:JsonIgnore
    val indicatorMap: RecipeIndicators by lazy {
        RecipeIndicators(indicators)
    }

    @get:JsonIgnore
    val mustUseMaterial: Predicate<String> by lazy {
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
                val thenOperator = thenCon.condition.operator
                val thenValue = thenCon.condition.value
                if (thenCon.materials.contains(materialId) && ((thenOperator == Operator.GT && thenValue >= 0) || (thenOperator == Operator.EQ && thenValue > 0))) {
                    return@Predicate true
                }
            }

            return@Predicate false
        }
    }

    //不用原料
    @get:JsonIgnore
    val nonUseMaterial = Predicate { material: IRecipeMaterial ->
        val materialId = material.id
        if (mustUseMaterial.test(materialId)) {
            return@Predicate false
        }

        // 过滤不使用的原料
        if (noUseMaterialConstraints.contains(materialId)) {
            return@Predicate true
        }

        // 排除全局非限用原料
        if (keepMaterialConstraints.ids.isNotEmpty()) {
            if (!keepMaterialConstraints.contains(materialId)) return@Predicate true
        }

        // 排除非限用原料
        materialIDConstraints.forEach { (t, u) ->
            if (t.contains(materialId) && !u.contains(materialId)) {
                return@Predicate true
            }
        }

        // 过滤不在成份约束的原料
        indicatorMaterialIDConstraints.values.forEach { indicator ->
            val materialIDs = indicator.value
            val materialIndicator = material.indicators[indicator.id]?.scaledValue ?: 0.0
            if (materialIndicator > 0 && !materialIDs.contains(materialId)) {
                return@Predicate true
            }
        }

        false
    }

    @get:JsonIgnore
    val useMaterials: List<RecipeMaterial>
        get() =
            (materials.filter { f -> mustUseMaterial.test(f.id) } + (materials - nonUseMaterials.toSet())).distinct()

    @get:JsonIgnore
    val nonUseMaterials: List<RecipeMaterial>
        get() = materials.filter { f -> nonUseMaterial.test(f) }

    init {
        init()
    }

    fun init() {
        if (targetWeight <= 0) {
            throw IllegalArgumentException("targetWeight must be greater than 0")
        }
        if (maxUseMaterialNum != null && maxUseMaterialNum <= 0) {
            throw IllegalArgumentException("maxUseMaterialNum must be greater than 0")
        }
        if (maxBakeWeight != null && maxBakeWeight < 0) {//不能小于0
            throw IllegalArgumentException("maxBakeWeight 不能小于0")
        }

        indicatorMaterialIDConstraints.init(indicatorMap)
        indicatorRangeConstraints.init(indicatorMap)
        materials.forEach {
            it.indicators.init(indicatorMap)
        }

        //约束原料
        materialRangeConstraints = materialRangeConstraints.filter { it.term.ids.isNotEmpty() }

        materialRelationConstraints.forEach {
            it.then = it.then.filter { t -> t.term.ids.isNotEmpty() }
        }
        materialRelationConstraints =
            materialRelationConstraints.filter { !it.term.isEmpty() }

        materialConditionConstraints =
            materialConditionConstraints.filter { it.term.materials.ids.isNotEmpty() && it.then.materials.ids.isNotEmpty() }

        //限用原料范围
        this.keepMaterialConstraints =
            if (keepMaterialConstraints.ids.isNotEmpty()) (materials.filter { f ->
                mustUseMaterial.test(
                    f.id
                )
            }.map { it.id } + keepMaterialConstraints.ids).distinct()
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

    }

}