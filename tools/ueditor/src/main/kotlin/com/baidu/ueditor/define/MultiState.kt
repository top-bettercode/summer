package com.baidu.ueditor.define

import com.baidu.ueditor.Encoder

/**
 * 多状态集合状态 其包含了多个状态的集合, 其本身自己也是一个状态
 *
 * @author hancong03@baidu.com
 */
class MultiState : State {
    override val isSuccess: Boolean
    private var info: String? = null
    private val intMap: MutableMap<String?, Long> = HashMap()
    private val infoMap: MutableMap<String?, String?> = HashMap()
    private val stateList: MutableList<String?> = ArrayList()

    constructor(state: Boolean) {
        isSuccess = state
    }

    constructor(state: Boolean, info: String?) {
        isSuccess = state
        this.info = info
    }

    constructor(state: Boolean, infoKey: Int) {
        isSuccess = state
        info = AppInfo.getStateInfo(infoKey)
    }

    fun addState(state: State) {
        stateList.add(state.toJSONString())
    }

    /**
     * 该方法调用无效果
     */
    override fun putInfo(name: String, `val`: String?) {
        infoMap[name] = `val`
    }

    override fun toJSONString(): String? {
        var stateVal = if (isSuccess) AppInfo.getStateInfo(AppInfo.SUCCESS) else info
        val builder = StringBuilder()
        builder.append("{\"state\": \"").append(stateVal).append("\"")

        // 数字转换
        var iterator: Iterator<String?> = intMap.keys.iterator()
        while (iterator.hasNext()) {
            stateVal = iterator.next()
            builder.append(",\"").append(stateVal).append("\": ").append(intMap[stateVal])
        }
        iterator = infoMap.keys.iterator()
        while (iterator.hasNext()) {
            stateVal = iterator.next()
            builder.append(",\"").append(stateVal).append("\": \"").append(infoMap[stateVal])
                    .append("\"")
        }
        builder.append(", list: [")
        iterator = stateList.iterator()
        while (iterator.hasNext()) {
            builder.append(iterator.next()).append(",")
        }
        if (stateList.size > 0) {
            builder.deleteCharAt(builder.length - 1)
        }
        builder.append(" ]}")
        return Encoder.toUnicode(builder.toString())
    }

    override fun putInfo(name: String, `val`: Long) {
        intMap[name] = `val`
    }
}
