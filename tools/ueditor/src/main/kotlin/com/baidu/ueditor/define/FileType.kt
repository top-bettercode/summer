package com.baidu.ueditor.define

import java.util.*

object FileType {
    const val JPG = "JPG"
    private val types: Map<String, String> = object : HashMap<String, String>() {
        private val serialVersionUID = 1L

        init {
            put(JPG, ".jpg")
        }
    }

    fun getSuffix(key: String): String? {
        return types[key]
    }

    /*
   * 根据给定的文件名,获取其后缀信息
   */
    fun getSuffixByFilename(filename: String): String {
        return filename.substring(filename.lastIndexOf(".")).lowercase(Locale.getDefault())
    }
}
