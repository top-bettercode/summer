package top.bettercode.summer.tools.hikvision.entity

/**
 * 分页数据
 */
class PageData<T> {
    /**
     * 当前页码
     */
    var pageNo: Int? = null

    /**
     * 单页展示数据数目
     */
    var pageSize: Int? = null

    /**
     * 总结果数
     */
    var total: Int? = null

    /**
     * 总页数
     */
    var totalPage: Int? = null

    /**
     * 返回数据集合
     */
    var list: List<T>? = null
}
