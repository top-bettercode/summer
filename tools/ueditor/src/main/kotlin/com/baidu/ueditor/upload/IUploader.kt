package com.baidu.ueditor.upload

import com.baidu.ueditor.define.State
import jakarta.servlet.http.HttpServletRequest

interface IUploader {
    fun doExec(request: HttpServletRequest?, conf: Map<String, Any?>?): State?
}
