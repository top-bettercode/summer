package com.baidu.ueditor

object Encoder {
    fun toUnicode(input: String): String {
        val builder = StringBuilder()
        val chars = input.toCharArray()
        for (ch in chars) {
            if (ch.code < 256) {
                builder.append(ch)
            } else {
                builder.append("\\u").append(Integer.toHexString(ch.code and 0xffff))
            }
        }
        return builder.toString()
    }
}