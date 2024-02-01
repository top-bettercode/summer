package top.bettercode.summer.tools.recipe

import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.criteria.RecipeRelation
import top.bettercode.summer.tools.recipe.indicator.RecipeMaterialIDIndicators
import top.bettercode.summer.tools.recipe.indicator.RecipeRangeIndicators
import top.bettercode.summer.tools.recipe.material.*
import top.bettercode.summer.tools.recipe.material.MaterialIDs.Companion.toMaterialIDs
import java.util.function.Predicate

/**
 * 配方要求
 * @author Peter Wu
 */
class RecipeRequirement(
        /** 产品名称  */
        val productName: String,
        /** 目标重量，单位KG  */
        val targetWeight: Double = 1000.0,
        /**
         * 收率
         */
        val yield: Double = 1.0,
        /** 原料进料口最大数，-1不限  */
        val maxUseMaterialNum: Int = -1,
        /**
         * 最大烘干量，单位KG，-1允许烘干全部水份
         */
        val maxBakeWeight: Double = -1.0,
        /** 原料  */
        var materials: List<RecipeMaterial>,
        /**
         * 指标指定用原料约束,key:指标ID,value:原料ID
         */
        val indicatorMaterialIDConstraints: RecipeMaterialIDIndicators,
        /**
         * 指定原料约束
         */
        val materialIDConstraints: Map<MaterialIDs, MaterialIDs>,
        /** 指定用原料ID  */
        val useMaterialConstraints: MaterialIDs,
        /** 不能用原料ID  */
        val noUseMaterialConstraints: MaterialIDs,
        /**
         * 指标范围约束,key：指标ID,value:指标值范围
         */
        val indicatorRangeConstraints: RecipeRangeIndicators,

        /** 不能混用的原料,value: 原料ID  */
        val notMixMaterialConstraints: List<Array<MaterialIDs>>,

        /** 原料约束,key:原料ID, value: 原料使用范围约束  */
        var materialRangeConstraints: Map<MaterialIDs, DoubleRange>,
        /**
         *关联原料约束
         */
        var materialRelationConstraints: Map<ReplacebleMaterialIDs, Map<RelationMaterialIDs, RecipeRelation>>,
        /** 条件约束，当条件1满足时，条件2必须满足  */
        val materialConditionConstraints: List<Pair<MaterialCondition, MaterialCondition>>
) {

    /** 超时时间  */
    var timeout = 30
    private val tmpMaterial = materials.associateBy { it.id }

    //--------------------------------------------

    init {
        //约束原料
        indicatorMaterialIDConstraints.values.forEach { indicator ->
            val materialIDs = indicator.value
            indicator.value = materialIDs.min()
        }

        materialRangeConstraints = materialRangeConstraints.mapKeys { (key, _) ->
            key.min()
        }
        materialRelationConstraints = materialRelationConstraints.mapKeys { (key, _) ->
            key.min()
        }.mapValues { (_, value) ->
            value.mapKeys { (key, _) ->
                key.min()
            }
        }
        materialConditionConstraints.forEach { (first, second) ->
            first.materials = first.materials.min()
            second.materials = second.materials.min()
        }

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
            for (replacebleMaterialIDs in materialRelationConstraints.keys) {
                if (replacebleMaterialIDs.contains(materialId)) {
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
        //不用原料
        val materialFalse = Predicate { material: IRecipeMaterial ->
            val materialId = material.id

            if (materialMust.test(material)) {
                return@Predicate true
            }

            // 条件 约束原料
            for (condition in materialConditionConstraints) {
                if (condition.second.materials.contains(materialId)) {
                    return@Predicate true
                }
            }

            // 过滤不使用的原料
            if (noUseMaterialConstraints.contains(materialId)) {
                return@Predicate false
            }

            // 排除全局非限用原料
            if (useMaterialConstraints.isNotEmpty()) {
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
        this.materials = materialList
    }


    private fun MaterialIDs.min(): MaterialIDs {
        val ids = this.mapNotNull { tmpMaterial[it] }.groupBy { it.indicators.key }.values.mapNotNull { list ->
            list.minOfWithOrNull(materialComparator) { it }?.id
        }
        return MaterialIDs(ids)
    }

    private fun RelationMaterialIDs.min(): RelationMaterialIDs {
        val ids = this.mapNotNull { tmpMaterial[it] }.groupBy { it.indicators.key }.values.mapNotNull { list ->
            list.minOfWithOrNull(materialComparator) { it }?.id
        }
        val relationIds = this.relationIds?.mapNotNull { tmpMaterial[it] }?.groupBy { it.indicators.key }?.values?.mapNotNull { list ->
            list.minOfWithOrNull(materialComparator) { it }?.id
        }?.toMaterialIDs()
        return RelationMaterialIDs(ids, relationIds)
    }

    private fun ReplacebleMaterialIDs.min(): ReplacebleMaterialIDs {
        val ids = this.mapNotNull { tmpMaterial[it] }.groupBy { it.indicators.key }.values.mapNotNull { list ->
            list.minOfWithOrNull(materialComparator) { it }?.id
        }
        val replaceIds = this.replaceIds?.mapNotNull { tmpMaterial[it] }?.groupBy { it.indicators.key }?.values?.mapNotNull { list ->
            list.minOfWithOrNull(materialComparator) { it }?.id
        }?.toMaterialIDs()
        return ReplacebleMaterialIDs(ids, replaceIds, this.replaceRate)
    }

    companion object {
        private val materialComparator: Comparator<RecipeMaterial> = Comparator { o1, o2 ->
            if (o1.price == o2.price)
                o1.index.compareTo(o2.index)
            else
                o1.price.compareTo(o2.price)
        }
    }

}