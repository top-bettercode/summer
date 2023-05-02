package com.baidu.ueditor.define

/**
 * 处理状态接口
 *
 * @author hancong03@baidu.com
 */
interface State {
    val isSuccess: Boolean
    fun putInfo(name: String, `val`: String?)
    fun putInfo(name: String, `val`: Long)
    fun toJSONString(): String?
}
