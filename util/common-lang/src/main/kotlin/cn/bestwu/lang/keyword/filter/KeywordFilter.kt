package cn.bestwu.lang.keyword.filter

/**
 * 关键字过滤器
 *
 * @author Peter Wu
 */
interface KeywordFilter {

    /**
     * 根据指定策略替换关键字，使用不同的策略可实现高亮功能。
     *
     * @param text 待匹配文本
     * @return 替换后的结果字符串
     */
    fun replace(text: String): String

    /**
     * 创建关键字搜索树
     *
     * @param keywords 关键字
     */
    fun compile(keywords: Collection<String>)
}
