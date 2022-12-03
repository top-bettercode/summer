package top.bettercode.summer.tools.weixin.support

interface DuplicatedMessageChecker {
    fun isDuplicated(msgKey: String): Boolean
}