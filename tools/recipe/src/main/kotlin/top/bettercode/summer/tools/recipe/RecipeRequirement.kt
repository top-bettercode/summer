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
        /** 原料进料口最大数，-1不限  */
        val maxUseMaterials: Int = -1,
        /**
         * 最大烘干量，单位KG，-1允许烘干全部水份
         */
        val maxBakeWeight: Double = -1.0,
        /** 原料  */
        materials: List<IRecipeMaterial>,
        /**
         * 指标范围约束
         */
        val rangeIndicators: RecipeRangeIndicators,
        /**
         * 指标物料约束
         */
        val materialIDIndicators: RecipeMaterialIDIndicators,

        /** 限用原料ID  */
        val useMaterials: MaterialIDs,
        /** 不使用的原料ID  */
        val noUseMaterials: MaterialIDs,
        /** 不能混用的原料,value: 原料ID  */
        val notMixMaterials: List<Array<MaterialIDs>>,

        /** 原料约束,key:原料ID, value: 原料使用范围约束  */
        var materialRangeConstraints: Map<MaterialIDs, DoubleRange>,
        /**
         * 指定物料约束
         */
        val materialIDConstraints: Map<MaterialIDs, MaterialIDs>,
        /**
         *关联物料约束
         */
        var materialRelationConstraints: Map<ReplacebleMaterialIDs, Map<MaterialIDs, RecipeRelation>>,
        /** 条件约束，当条件1满足时，条件2必须满足  */
        val materialConditions: List<Pair<MaterialCondition, MaterialCondition>>
) {
    val materials: RecipeMaterials

    /** 超时时间  */
    var timeout = 30

    //--------------------------------------------

    init {
        val tmpMaterials = RecipeMaterials(materials)
        //约束物料
        materialIDIndicators.values.forEach { indicator ->
            val materialIDs = indicator.value
            indicator.value = materialIDs.min(tmpMaterials)
        }
        materialRangeConstraints = materialRangeConstraints.mapKeys { (key, _) ->
            key.min(tmpMaterials)
        }
        materialRelationConstraints = materialRelationConstraints.mapKeys { (key, _) ->
            key.min(tmpMaterials)
        }.mapValues { (_, value) ->
            value.mapKeys { (key, _) ->
                key.min(tmpMaterials)
            }
        }
        materialConditions.forEach { (first, second) ->
            first.materials = first.materials.min(tmpMaterials)
            second.materials = second.materials.min(tmpMaterials)
        }

        // 必选物料
        val materialMust = Predicate { material: IRecipeMaterial ->
            val materialId = material.id

            // 用量>0的原料
            materialRangeConstraints.forEach { (t, u) ->
                if (t.contains(materialId) && u.min > 0) {
                    return@Predicate true
                }
            }

            //关联物料
            for (replacebleMaterialIDs in materialRelationConstraints.keys) {
                if (replacebleMaterialIDs.contains(materialId)) {
                    return@Predicate true
                }
            }

            //条件约束
            materialConditions.forEach { (_, thenCon) ->
                if (thenCon.materials.contains(materialId)) {
                    return@Predicate true
                }
            }

            return@Predicate false
        }
        //不用物料
        val materialFalse = Predicate { material: IRecipeMaterial ->
            val materialId = material.id

            if (materialMust.test(material)) {
                return@Predicate true
            }

            // 条件 约束物料
            for (condition in materialConditions) {
                if (condition.second.materials.contains(materialId)) {
                    return@Predicate true
                }
            }

            // 过滤不使用的原料
            if (noUseMaterials.contains(materialId)) {
                return@Predicate false
            }

            // 排除全局非限用物料
            if (useMaterials.isNotEmpty()) {
                if (!useMaterials.contains(materialId)) return@Predicate false
            }

            // 排除非限用物料
            materialIDConstraints.forEach { (t, u) ->
                if (t.contains(materialId) && !u.contains(materialId)) {
                    return@Predicate false
                }
            }

            // 过滤不在成份约束的原料
            materialIDIndicators.values.forEach { indicator ->
                val materialIDs = indicator.value
                val materialIndicator = material.indicators.valueOf(indicator.index)
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
        this.materials = RecipeMaterials(materialList)
    }


    private fun MaterialIDs.min(materials: RecipeMaterials): MaterialIDs {
        val ids = this.mapNotNull { materials[it] }.groupBy { it.indicators.key }.values.mapNotNull { list ->
            list.minOfWithOrNull(materialComparator) { it }?.id
        }
        return MaterialIDs(ids.toSet())
    }

    private fun ReplacebleMaterialIDs.min(materials: RecipeMaterials): ReplacebleMaterialIDs {
        val ids = this.mapNotNull { materials[it] }.groupBy { it.indicators.key }.values.mapNotNull { list ->
            list.minOfWithOrNull(materialComparator) { it }?.id
        }
        val replaceIds = this.replaceIds?.mapNotNull { materials[it] }?.groupBy { it.indicators.key }?.values?.mapNotNull { list ->
            list.minOfWithOrNull(materialComparator) { it }?.id
        }?.toMaterialIDs()
        return ReplacebleMaterialIDs(ids, replaceIds, this.replaceRate)
    }

    companion object {
        private val materialComparator: Comparator<IRecipeMaterial> = Comparator { o1, o2 ->
            if (o1.price == o2.price)
                o1.index.compareTo(o2.index)
            else
                o1.price.compareTo(o2.price)
        }
    }

}