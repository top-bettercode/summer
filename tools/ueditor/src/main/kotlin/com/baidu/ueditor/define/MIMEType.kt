package com.baidu.ueditor.define

object MIMEType {
    val types: Map<String, String> = object : HashMap<String, String>() {
        private val serialVersionUID = 1L

        init {
            put("image/gif", ".gif")
            put("image/jpeg", ".jpg")
            put("image/jpg", ".jpg")
            put("image/png", ".png")
            put("image/bmp", ".bmp")
        }
    }

    fun getSuffix(mime: String): String? {
        return types[mime]
    }
}
