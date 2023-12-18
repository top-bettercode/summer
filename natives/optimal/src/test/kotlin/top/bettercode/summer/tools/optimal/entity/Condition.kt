package top.bettercode.summer.tools.optimal.entity


/**
 * 限制条件
 *
 * @author Peter Wu
 */
class Condition(var desc: String) {
    /** 原料名称片段  */
    var materialNameFragment: String

    /** 操作类型： = : 0 ,>:1,<:2 ,>=:3 ,<=:4 </:2> */
    var type: Int? = null

    /** 值  */
    var value: Double

    init {
        val split = desc.split(">=|<=|=|>|<".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        materialNameFragment = split[0]
        value = split[1].toDouble()
        val typeStr = desc.replace(".*(=|>|<|>=|<=).*".toRegex(), "$1")
        type = when (typeStr) {
            "=" -> 0
            ">" -> 1
            "<" -> 2
            ">=" -> 3
            "<=" -> 4
            else -> 0
        }
    }
}
