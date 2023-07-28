package com.baidu.ueditor

import com.baidu.ueditor.define.ActionMap
import com.baidu.ueditor.define.AppInfo
import com.baidu.ueditor.define.BaseState
import com.baidu.ueditor.define.State
import com.baidu.ueditor.hunter.FileManager
import com.baidu.ueditor.upload.IUploader
import org.springframework.util.Assert
import javax.servlet.http.HttpServletRequest

class ActionEnter {
    private val configManager: ConfigManager? = ConfigManager.instance

    fun exec(request: HttpServletRequest, uploader: IUploader): String? {
        val callbackName = request.getParameter("callback")
        return if (callbackName != null) {
            if (!validCallbackName(callbackName)) {
                BaseState(false, AppInfo.ILLEGAL).toJSONString()
            } else callbackName + "(" + this.invoke(request, uploader) + ");"
        } else {
            this.invoke(request, uploader)
        }
    }

    private operator fun invoke(request: HttpServletRequest, uploader: IUploader): String? {
        val actionType = request.getParameter("action")
        if (actionType == null || !ActionMap.mapping.containsKey(actionType)) {
            return BaseState(false, AppInfo.INVALID_ACTION).toJSONString()
        }
        if (configManager == null || !configManager.valid()) {
            return BaseState(false, AppInfo.CONFIG_ERROR).toJSONString()
        }
        var state: State? = null
        val actionCode = ActionMap.getType(actionType)
        val conf: Map<String, Any?>?
        when (actionCode) {
            ActionMap.CONFIG -> return configManager.allConfig.toString()
            ActionMap.UPLOAD_IMAGE, ActionMap.UPLOAD_SCRAWL, ActionMap.UPLOAD_VIDEO, ActionMap.UPLOAD_FILE, ActionMap.CATCH_IMAGE -> {
                conf = configManager.getConfig(actionCode)
                state = uploader.doExec(request, conf)
            }

            ActionMap.LIST_IMAGE, ActionMap.LIST_FILE -> {
                conf = configManager.getConfig(actionCode)
                val start = getStartIndex(request)
                state = FileManager(conf).listFile(start)
            }
        }
        Assert.notNull(state, "state 异常")
        return state!!.toJSONString()
    }

    private fun getStartIndex(request: HttpServletRequest): Int {
        val start = request.getParameter("start")
        return try {
            start.toInt()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * callback参数验证
     */
    private fun validCallbackName(name: String): Boolean {
        return name.matches("^[a-zA-Z_]+[\\w0-9_]*$".toRegex())
    }
}