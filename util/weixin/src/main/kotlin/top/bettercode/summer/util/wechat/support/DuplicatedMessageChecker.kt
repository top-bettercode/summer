package top.bettercode.summer.util.wechat.support

interface DuplicatedMessageChecker {
    fun isDuplicated(msgKey: String): Boolean
}