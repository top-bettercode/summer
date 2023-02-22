package top.bettercode.summer.web.support.gb2260

import com.fasterxml.jackson.annotation.JsonView

/**
 * 行政区
 */
data class Division(
    /**
     * 代码
     */
    @JsonView(DivisionView::class)
    val code: String,
    /**
     * 名称
     */
    @JsonView(DivisionView::class)
    val name: String,
    /**
     * 节点层级，从1开始
     */
    val level: Int,
    /**
     * 父节点名称
     */
    @JsonView(DivisionView::class)
    val parentNames: List<String>,
    /**
     * 子节点
     */
    @JsonView(AllDivisionView::class)
    val children: List<Division>
) {

    /**
     * 是否叶子节点
     */
    @JsonView(DivisionView::class)
    val leaf: Boolean = children.isEmpty()

    /**
     * 省
     */
    val province: String = if (level == 1) name else parentNames.first()

    /**
     * 市
     */
    val prefecture: String? = when (level) {
        1 -> null
        3 -> parentNames.last()
        else -> name
    }

    /**
     * 区县
     */
    val county: String? = if (level == 3) name else null


    override fun toString(): String {
        return "${parentNames.joinToString("")}$name"
    }
}