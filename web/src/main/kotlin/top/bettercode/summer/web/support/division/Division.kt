package top.bettercode.summer.web.support.division

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
         * 是否直辖市
         */
        val municipality: Boolean,
        val vnode: Boolean,
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
    @get:JsonView(DivisionView::class)
    val leaf: Boolean by lazy { children.isEmpty() }


    /**
     * 省
     */
    val province: String by lazy { if (level == 1) name else parentNames.first() }

    /**
     * 市
     */
    val prefecture: String? by lazy {
        when (level) {
            1 -> null
            2 -> if (vnode) parentNames.first() else name
            else -> parentNames.last()
        }
    }

    /**
     * 区县
     */
    val county: String? by lazy { if (level == 3) name else null }

    /**
     * 代码
     */
    fun codes(vnode: Boolean): List<String> = when (level) {
        1 -> listOf(code)
        2 -> listOf(code.substring(0, 2) + "0000", code)
        else -> if (!vnode && parentNames.size == 1)
            listOf(code.substring(0, 2) + "0000", code)
        else listOf(
                code.substring(0, 2) + "0000",
                code.substring(0, 4) + "00",
                code
        )
    }

    /**
     * 名称
     */
    val names: List<String> by lazy { parentNames + name }

    override fun toString(): String {
        return "${parentNames.joinToString("")}$name"
    }
}