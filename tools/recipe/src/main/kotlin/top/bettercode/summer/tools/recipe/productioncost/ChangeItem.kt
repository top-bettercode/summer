package top.bettercode.summer.tools.recipe.productioncost

/** 制造费用增减结果项 */
class ChangeItem(
        /** 类型  */
        val type: ChangeItemType,

        /** ID,对应原料品类/制造费用项标识  */
        val id: String
)