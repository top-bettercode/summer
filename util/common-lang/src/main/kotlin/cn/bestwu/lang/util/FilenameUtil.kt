package cn.bestwu.lang.util

import java.io.File

/**
 * 文件名工具
 *
 * @author Peter Wu
 */
object FilenameUtil {

    @JvmStatic
    fun getExtension(file: File): String {
        return file.extension
    }

    @JvmStatic
    fun getNameWithoutExtension(file: File): String {
        return file.nameWithoutExtension
    }

    @JvmStatic
    fun getExtension(fileName: String): String {
        return File(fileName).extension
    }

    @JvmStatic
    fun getNameWithoutExtension(fileName: String): String {
        return File(fileName).nameWithoutExtension
    }
}
