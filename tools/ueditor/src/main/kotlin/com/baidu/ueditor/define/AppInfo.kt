package com.baidu.ueditor.define

object AppInfo {
    const val SUCCESS = 0
    const val MAX_SIZE = 1
    const val PERMISSION_DENIED = 2
    const val FAILED_CREATE_FILE = 3
    const val IO_ERROR = 4
    const val NOT_MULTIPART_CONTENT = 5
    const val PARSE_REQUEST_ERROR = 6
    const val NOTFOUND_UPLOAD_DATA = 7
    const val NOT_ALLOW_FILE_TYPE = 8
    const val INVALID_ACTION = 101
    const val CONFIG_ERROR = 102
    const val PREVENT_HOST = 201
    const val CONNECTION_ERROR = 202
    const val REMOTE_FAIL = 203
    const val NOT_DIRECTORY = 301
    const val NOT_EXIST = 302
    const val ILLEGAL = 401
    val info: Map<Int, String> = object : HashMap<Int, String>() {
        private val serialVersionUID = 1L

        init {
            put(SUCCESS, "SUCCESS")

            // 无效的Action
            put(INVALID_ACTION, "无效的Action")
            // 配置文件初始化失败
            put(CONFIG_ERROR, "配置文件初始化失败")
            // 抓取远程图片失败
            put(REMOTE_FAIL, "抓取远程图片失败")

            // 被阻止的远程主机
            put(PREVENT_HOST, "被阻止的远程主机")
            // 远程连接出错
            put(CONNECTION_ERROR, "远程连接出错")

            // "文件大小超出限制"
            put(MAX_SIZE, "文件大小超出限制")
            // 权限不足， 多指写权限
            put(PERMISSION_DENIED, "权限不足")
            // 创建文件失败
            put(FAILED_CREATE_FILE, "创建文件失败")
            // IO错误
            put(IO_ERROR, "IO错误")
            // 上传表单不是multipart/form-data类型
            put(NOT_MULTIPART_CONTENT,
                    "上传表单不是multipart/form-data类型")
            // 解析上传表单错误
            put(PARSE_REQUEST_ERROR, "解析上传表单错误")
            // 未找到上传数据
            put(NOTFOUND_UPLOAD_DATA, "未找到上传数据")
            // 不允许的文件类型
            put(NOT_ALLOW_FILE_TYPE, "不允许的文件类型")

            // 指定路径不是目录
            put(NOT_DIRECTORY, "指定路径不是目录")
            // 指定路径并不存在
            put(NOT_EXIST, "指定路径并不存在")

            // callback参数名不合法
            put(ILLEGAL, "Callback参数名不合法")
        }
    }

    fun getStateInfo(key: Int): String? {
        return info[key]
    }
}
