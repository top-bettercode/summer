package cn.bestwu.lang.keyword.filter

import cn.bestwu.lang.keyword.CharNode
import cn.bestwu.lang.keyword.MatchType
import cn.bestwu.lang.keyword.replace.DefaultReplaceStrategy
import cn.bestwu.lang.keyword.replace.ReplaceStrategy
import java.util.*

/**
 * 简单实现
 *
 * @author Peter Wu
 */
open class SimpleKeywordFilter(
        val root: CharNode = CharNode(),
        /**
         * 设置匹配模式
         */
        var matchType: MatchType = MatchType.LONG,
        /**
         * 设置替换策略
         */
        var strategy: ReplaceStrategy = DefaultReplaceStrategy()) : KeywordFilter {


    override fun replace(text: String): String {
        var last = root
        val result = StringBuilder()
        val words = text.toCharArray()
        val matchShort = matchType == MatchType.SHORT
        var i = 0
        while (i < words.size) {
            val word = words[i]

            var length = last.length
            val lastIndex = i - length
            val end = i == words.size - 1
            var containLast = false
            val charNode = last[word]
            if (charNode != null) {
                last = charNode
                length++
                containLast = true
            }
            val lastEnd = last.isEnd
            if (last === root) {
                result.append(word)
            } else if (containLast && matchShort && lastEnd) {
                result
                        .append(strategy.replaceWith(Arrays.copyOfRange(words, lastIndex, lastIndex + length)))
                last = root
            } else if (!containLast || end) {
                if (lastEnd) {
                    result.append(strategy
                            .replaceWith(Arrays.copyOfRange(words, lastIndex, lastIndex + length)))
                    if (!containLast) {
                        i--
                    }
                } else {
                    // 未结束，找短匹配
                    if (matchShort) {
                        i = lastIndex
                        result.append(words[i])
                    } else {
                        val failNode = last.failNode
                        if (failNode === root) {
                            i = lastIndex
                            result.append(words[i])
                        } else {
                            val failLength = failNode!!.length
                            i = lastIndex + failLength - 1
                            result.append(strategy.replaceWith(Arrays.copyOfRange(words,
                                    lastIndex, lastIndex + failLength)))
                        }
                    }
                }
                last = root
            }
            i++
        }
        return result.toString()
    }

    override fun compile(keywords: Collection<String>) {
        addKeywords(keywords)
        // 构建失败节点
        buildFailNode(root)
    }

    /**
     * 构建char树，作为搜索的数据结构。
     *
     * @param keywords 关键字
     */
    open fun addKeywords(keywords: Collection<String>) {
        // 加入关键字字符串
        for (keyword in keywords) {
            if (keyword.isBlank()) {
                throw IllegalArgumentException("过滤关键词不能为空！")
            }
            val charArray = keyword.toCharArray()
            var node = root
            for (aCharArray in charArray) {
                node = node.addChild(aCharArray)
            }
        }
    }

    /**
     * 构建失败节点
     *
     * @param node 节点
     */
    open fun buildFailNode(node: CharNode) {
        doFailNode(node)
        val childNodes = node.childNodes()
        for (childNode in childNodes) {
            buildFailNode(childNode)
        }
    }

    private fun doFailNode(node: CharNode) {
        if (node === root) {
            return
        }
        var parent = node.parent

        while (!parent!!.isEnd && parent !== root) {
            parent = parent.parent
        }
        node.failNode = parent
    }

}
