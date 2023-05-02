package com.baidu.ueditor.define

import com.baidu.ueditor.Encoder

class BaseState : State {
    override var isSuccess = false
        private set
    private var info: String? = null
    private val infoMap: MutableMap<String, String?> = HashMap()

    constructor() {
        isSuccess = true
    }

    constructor(state: Boolean) {
        setState(state)
    }

    constructor(state: Boolean, info: String?) {
        setState(state)
        this.info = info
    }

    constructor(state: Boolean, infoCode: Int) {
        setState(state)
        info = AppInfo.getStateInfo(infoCode)
    }

    fun setState(state: Boolean) {
        isSuccess = state
    }

    fun setInfo(info: String?) {
        this.info = info
    }

    fun setInfo(infoCode: Int) {
        info = AppInfo.getStateInfo(infoCode)
    }

    override fun toJSONString(): String? {
        return this.toString()
    }

    override fun toString(): String {
        var key: String
        val stateVal = if (isSuccess) AppInfo.getStateInfo(AppInfo.SUCCESS) else info
        val builder = StringBuilder()
        builder.append("{\"state\": \"").append(stateVal).append("\"")
        for (s in infoMap.keys) {
            key = s
            builder.append(",\"").append(key).append("\": \"").append(infoMap[key]).append("\"")
        }
        builder.append("}")
        return Encoder.toUnicode(builder.toString())
    }

    override fun putInfo(name: String, `val`: String?) {
        infoMap[name] = `val`
    }

    override fun putInfo(name: String, `val`: Long) {
        this.putInfo(name, `val`.toString() + "")
    }
}
