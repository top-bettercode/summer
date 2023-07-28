package com.baidu.ueditor.define

/**
 * 定义请求action类型
 *
 * @author hancong03@baidu.com
 */
object ActionMap {
    val mapping: Map<String, Int>

    // 获取配置请求
    const val CONFIG = 0
    const val UPLOAD_IMAGE = 1
    const val UPLOAD_SCRAWL = 2
    const val UPLOAD_VIDEO = 3
    const val UPLOAD_FILE = 4
    const val CATCH_IMAGE = 5
    const val LIST_FILE = 6
    const val LIST_IMAGE = 7

    init {
        mapping = mapOf(
                "config" to CONFIG,
                "uploadimage" to UPLOAD_IMAGE,
                "uploadscrawl" to UPLOAD_SCRAWL,
                "uploadvideo" to UPLOAD_VIDEO,
                "uploadfile" to UPLOAD_FILE,
                "catchimage" to CATCH_IMAGE,
                "listfile" to LIST_FILE,
                "listimage" to LIST_IMAGE
        )
    }

    @JvmStatic
    fun getType(key: String): Int? {
        return mapping[key]
    }
}
