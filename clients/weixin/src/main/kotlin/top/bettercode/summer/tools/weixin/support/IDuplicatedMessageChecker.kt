package top.bettercode.summer.tools.weixin.support

interface IDuplicatedMessageChecker {
    fun isDuplicated(msgKey: String): Boolean
}