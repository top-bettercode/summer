package top.bettercode.summer.web.support.gb2260

import com.fasterxml.jackson.annotation.JsonView

/**
 * 行政区
 */
data class Division(
    @JsonView(DivisionView::class)
    val code: String,
    @JsonView(DivisionView::class)
    val name: String,
    @JsonView(DivisionView::class)
    val parentNames: List<String>,
    val children: List<Division>
) {

    val leaf: Boolean
        @JsonView(DivisionView::class)
        get() = children.isEmpty()

    override fun toString(): String {
        return "${parentNames.joinToString("")}$name"
    }
}