package top.bettercode.summer.tools.recipe.productioncost

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.recipe.RecipeRequirement

/** 制造费用增减结果项 */
@JsonPropertyOrder(alphabetic = true)
data class ChangeItem(
    /** 类型  */
    @JsonProperty("type")
    val type: ChangeItemType,

    /** ID,对应原料品类/制造费用项标识  */
    @JsonProperty("id")
    val id: String
) {

    fun toName(requirement: RecipeRequirement): String {
        return when (type) {
            ChangeItemType.MATERIAL -> requirement.materials.find { it.id == id }?.name ?: id
            ChangeItemType.DICT -> {
                DictType.valueOf(id).dictName
            }
        }
    }
}