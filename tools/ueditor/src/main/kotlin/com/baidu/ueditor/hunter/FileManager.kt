package com.baidu.ueditor.hunter

import com.baidu.ueditor.PathFormat
import com.baidu.ueditor.define.AppInfo
import com.baidu.ueditor.define.BaseState
import com.baidu.ueditor.define.MultiState
import com.baidu.ueditor.define.State
import top.bettercode.summer.tools.lang.util.ArrayUtil.contains
import top.bettercode.summer.tools.lang.util.FileUtil.listFiles
import java.io.File
import java.util.*

class FileManager(conf: Map<String, Any?>?) {
    private val dir: String
    private val rootPath: String?
    private val allowFiles: Array<String>
    private val count: Int

    init {
        rootPath = conf!!["rootPath"] as String?
        dir = rootPath + conf["dir"]
        allowFiles = getAllowFiles(conf["allowFiles"])
        count = (conf["count"] as Int?)!!
    }

    fun listFile(index: Int): State {
        val dir = File(dir)
        val state: State
        if (!dir.exists()) {
            return BaseState(false, AppInfo.NOT_EXIST)
        }
        if (!dir.isDirectory) {
            return BaseState(false, AppInfo.NOT_DIRECTORY)
        }
        val list = listFiles(dir,
                { pathname: File -> allowFiles.contains(pathname.path) }, true)
        state = if (index < 0 || index > list.size) {
            MultiState(true)
        } else {
            val fileList = Arrays.copyOfRange<Any>(list.toTypedArray(), index, index + count)
            getState(fileList)
        }
        state.putInfo("start", index.toLong())
        state.putInfo("total", list.size.toLong())
        return state
    }

    private fun getState(files: Array<Any?>): State {
        val state = MultiState(true)
        var fileState: BaseState
        var file: File
        for (obj in files) {
            if (obj == null) {
                break
            }
            file = obj as File
            fileState = BaseState(true)
            fileState.putInfo("url", PathFormat.format(getPath(file)))
            state.addState(fileState)
        }
        return state
    }

    private fun getPath(file: File): String {
        val path = file.absolutePath
        return path.replace(rootPath!!, "/")
    }

    private fun getAllowFiles(fileExt: Any?): Array<String> {
        val exts: Array<String>
        var ext: String?
        if (fileExt == null) {
            return arrayOf()
        }
        @Suppress("UNCHECKED_CAST")
        exts = fileExt as Array<String>
        var i = 0
        val len = exts.size
        while (i < len) {
            ext = exts[i]
            exts[i] = ext.replace(".", "")
            i++
        }
        return exts
    }
}
