package cn.bestwu.lang.keyword.replace

import java.util.*

/**
 * 默认替换策略，匹配的字符替换为“*”
 *
 * @author Peter Wu
 */
class DefaultReplaceStrategy : ReplaceStrategy {

    override fun replaceWith(words: CharArray): CharArray {
        Arrays.fill(words, '*')
        return words
    }

}
