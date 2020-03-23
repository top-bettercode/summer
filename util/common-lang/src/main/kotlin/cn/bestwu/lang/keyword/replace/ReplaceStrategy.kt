package cn.bestwu.lang.keyword.replace

/**
 * 关键字替换策略
 *
 * @author Peter Wu
 */
interface ReplaceStrategy {

    /**
     * 将关键字替换为期望的结果字符串
     *
     * @param words 匹配到的关键字
     * @return The resulting <tt>String</tt>
     */
    fun replaceWith(words: CharArray): CharArray

}
