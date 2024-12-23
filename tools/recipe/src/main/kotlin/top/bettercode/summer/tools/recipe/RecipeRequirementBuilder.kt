package top.bettercode.summer.tools.recipe

import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.criteria.RecipeRelation
import top.bettercode.summer.tools.recipe.criteria.TermThen
import top.bettercode.summer.tools.recipe.indicator.RecipeIndicator
import top.bettercode.summer.tools.recipe.indicator.RecipeMaterialIDIndicators
import top.bettercode.summer.tools.recipe.indicator.RecipeRangeIndicators
import top.bettercode.summer.tools.recipe.material.MaterialCondition
import top.bettercode.summer.tools.recipe.material.RecipeMaterial
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs
import top.bettercode.summer.tools.recipe.material.id.RelationMaterialIDs
import top.bettercode.summer.tools.recipe.material.id.ReplacebleMaterialIDs
import top.bettercode.summer.tools.recipe.productioncost.ProductionCost
import java.util.*

/**
 * 配方要求
 * @author Peter Wu
 */
class RecipeRequirementBuilder(
    /**
     * id
     */
    var id: String,
) {

    /** 产品名称  */
    var productName: String = id

    /** 目标重量，单位KG  */
    var targetWeight: Double = 1000.0

    /**
     * 收率
     */
    var yield: Double = 1.0

    /** 原料进料口最大数，null不限  */
    var maxUseMaterialNum: Int? = null

    /**
     * 最大烘干量，单位KG，null 允许烘干全部水份
     */
    var maxBakeWeight: Double? = null

    /**
     * 制造费用
     */
    var productionCost: ProductionCost = ProductionCost(
        materialItems = emptyList(), dictItems = TreeMap(), taxRate = 0.0, taxFloat = 0.0,
        changes = emptyList()
    )

    /**
     *指标
     */
    var indicators: List<RecipeIndicator> = emptyList()

    /**
     * 包装耗材
     */
    var packagingMaterials: List<RecipeOtherMaterial> = emptyList()

    /** 原料  */
    var materials: List<RecipeMaterial> = emptyList()

    /** 保留用原料ID  */
    var keepMaterialConstraints: MaterialIDs = MaterialIDs()

    /** 不能用原料ID  */
    var noUseMaterialConstraints: MaterialIDs = MaterialIDs()

    /**
     * 指标范围约束key：指标IDvalue:指标值范围
     */
    var indicatorRangeConstraints: RecipeRangeIndicators = RecipeRangeIndicators()

    /** 原料约束key:原料ID value: 原料使用范围约束  */
    var materialRangeConstraints: List<TermThen<MaterialIDs, DoubleRange>> = emptyList()

    /** 条件约束，当条件1满足时，条件2必须满足  */
    var materialConditionConstraints: List<TermThen<MaterialCondition, MaterialCondition>> =
        emptyList()

    /**
     *关联原料约束term:消耗的原料，then:关联的原料（trem 原料(relation 消耗此原料的原料)，then:关联的指标）
     */
    var materialRelationConstraints: List<TermThen<ReplacebleMaterialIDs, List<TermThen<RelationMaterialIDs, RecipeRelation>>>> =
        emptyList()

    /**
     * 指定原料约束
     */
    var materialIDConstraints: List<TermThen<MaterialIDs, MaterialIDs>> = emptyList()

    /**
     * 指标指定用原料约束key:指标IDvalue:原料ID
     */
    var indicatorMaterialIDConstraints: RecipeMaterialIDIndicators =
        RecipeMaterialIDIndicators.EMPTY

    /** 不能混用的原料value: 原料ID  */
    var notMixMaterialConstraints: List<Array<MaterialIDs>> = emptyList()

    /**
     * 指标小数位
     */
    var indicatorScale: Int = 5

    /** 超时时间,单位秒  */
    var timeout: Long = 30L

    fun productName(productName: String): RecipeRequirementBuilder {
        this.productName = productName
        return this
    }

    fun targetWeight(targetWeight: Double): RecipeRequirementBuilder {
        this.targetWeight = targetWeight
        return this
    }

    fun yield(`yield`: Double): RecipeRequirementBuilder {
        this.`yield` = `yield`
        return this
    }

    fun maxUseMaterialNum(maxUseMaterialNum: Int?): RecipeRequirementBuilder {
        this.maxUseMaterialNum = maxUseMaterialNum
        return this
    }

    fun maxBakeWeight(maxBakeWeight: Double?): RecipeRequirementBuilder {
        this.maxBakeWeight = maxBakeWeight
        return this
    }

    fun productionCost(productionCost: ProductionCost): RecipeRequirementBuilder {
        this.productionCost = productionCost
        return this
    }

    fun indicators(indicators: List<RecipeIndicator>): RecipeRequirementBuilder {
        this.indicators = indicators
        return this
    }

    fun packagingMaterials(packagingMaterials: List<RecipeOtherMaterial>): RecipeRequirementBuilder {
        this.packagingMaterials = packagingMaterials
        return this
    }

    fun materials(materials: List<RecipeMaterial>): RecipeRequirementBuilder {
        this.materials = materials
        return this
    }

    fun keepMaterialConstraints(keepMaterialConstraints: MaterialIDs): RecipeRequirementBuilder {
        this.keepMaterialConstraints = keepMaterialConstraints
        return this
    }

    fun noUseMaterialConstraints(noUseMaterialConstraints: MaterialIDs): RecipeRequirementBuilder {
        this.noUseMaterialConstraints = noUseMaterialConstraints
        return this
    }

    fun indicatorRangeConstraints(indicatorRangeConstraints: RecipeRangeIndicators): RecipeRequirementBuilder {
        this.indicatorRangeConstraints = indicatorRangeConstraints
        return this
    }

    fun materialRangeConstraints(materialRangeConstraints: List<TermThen<MaterialIDs, DoubleRange>>): RecipeRequirementBuilder {
        this.materialRangeConstraints = materialRangeConstraints
        return this
    }

    fun materialConditionConstraints(materialConditionConstraints: List<TermThen<MaterialCondition, MaterialCondition>>): RecipeRequirementBuilder {
        this.materialConditionConstraints = materialConditionConstraints
        return this
    }

    fun materialRelationConstraints(materialRelationConstraints: List<TermThen<ReplacebleMaterialIDs, List<TermThen<RelationMaterialIDs, RecipeRelation>>>>): RecipeRequirementBuilder {
        this.materialRelationConstraints = materialRelationConstraints
        return this
    }

    fun materialIDConstraints(materialIDConstraints: List<TermThen<MaterialIDs, MaterialIDs>>): RecipeRequirementBuilder {
        this.materialIDConstraints = materialIDConstraints
        return this
    }

    fun indicatorMaterialIDConstraints(indicatorMaterialIDConstraints: RecipeMaterialIDIndicators): RecipeRequirementBuilder {
        this.indicatorMaterialIDConstraints = indicatorMaterialIDConstraints
        return this
    }

    fun notMixMaterialConstraints(notMixMaterialConstraints: List<Array<MaterialIDs>>): RecipeRequirementBuilder {
        this.notMixMaterialConstraints = notMixMaterialConstraints
        return this
    }

    fun indicatorScale(indicatorScale: Int): RecipeRequirementBuilder {
        this.indicatorScale = indicatorScale
        return this
    }

    fun timeout(timeout: Long): RecipeRequirementBuilder {
        this.timeout = timeout
        return this
    }

    fun build(): RecipeRequirement {
        return RecipeRequirement(
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
            notMixMaterialConstraints = notMixMaterialConstraints,
            indicatorScale = indicatorScale,
            timeout = timeout
        )
    }
}