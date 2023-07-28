package com.baidu.ueditor

import com.baidu.ueditor.define.ActionMap
import org.json.JSONObject
import org.springframework.core.io.ClassPathResource
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets

/**
 * 配置管理器
 *
 * @author hancong03@baidu.com
 */
class ConfigManager private constructor() {
    var allConfig: JSONObject? = null

    /*
   * 通过一个给定的路径构建一个配置管理器， 该管理器要求地址路径所在目录下必须存在config.properties文件
   */
    init {
        val configContent = readFile(ClassPathResource(configFileName).inputStream)
        allConfig = try {
            JSONObject(configContent)
        } catch (e: Exception) {
            null
        }
    }

    // 验证配置文件加载是否正确
    fun valid(): Boolean {
        return allConfig != null
    }

    fun getConfig(type: Int): Map<String, Any?> {
        val conf: MutableMap<String, Any?> = HashMap()
        var savePath: String? = null
        when (type) {
            ActionMap.UPLOAD_FILE -> {
                conf["isBase64"] = "false"
                conf["maxSize"] = allConfig?.getLong("fileMaxSize")
                conf["allowFiles"] = getArray("fileAllowFiles")
                conf["fieldName"] = allConfig?.getString("fileFieldName")
                savePath = allConfig?.getString("filePathFormat")
            }

            ActionMap.UPLOAD_IMAGE -> {
                conf["isBase64"] = "false"
                conf["maxSize"] = allConfig?.getLong("imageMaxSize")
                conf["allowFiles"] = getArray("imageAllowFiles")
                conf["fieldName"] = allConfig?.getString("imageFieldName")
                savePath = allConfig?.getString("imagePathFormat")
            }

            ActionMap.UPLOAD_VIDEO -> {
                conf["maxSize"] = allConfig?.getLong("videoMaxSize")
                conf["allowFiles"] = getArray("videoAllowFiles")
                conf["fieldName"] = allConfig?.getString("videoFieldName")
                savePath = allConfig?.getString("videoPathFormat")
            }

            ActionMap.UPLOAD_SCRAWL -> {
                conf["filename"] = SCRAWL_FILE_NAME
                conf["maxSize"] = allConfig?.getLong("scrawlMaxSize")
                conf["fieldName"] = allConfig?.getString("scrawlFieldName")
                conf["isBase64"] = "true"
                savePath = allConfig?.getString("scrawlPathFormat")
            }

            ActionMap.CATCH_IMAGE -> {
                conf["filename"] = REMOTE_FILE_NAME
                conf["filter"] = getArray("catcherLocalDomain")
                conf["maxSize"] = allConfig?.getLong("catcherMaxSize")
                conf["allowFiles"] = getArray("catcherAllowFiles")
                conf["fieldName"] = allConfig?.getString("catcherFieldName") + "[]"
                savePath = allConfig?.getString("catcherPathFormat")
            }

            ActionMap.LIST_IMAGE -> {
                conf["allowFiles"] = getArray("imageManagerAllowFiles")
                conf["dir"] = allConfig?.getString("imageManagerListPath")
                conf["count"] = allConfig?.getInt("imageManagerListSize")
            }

            ActionMap.LIST_FILE -> {
                conf["allowFiles"] = getArray("fileManagerAllowFiles")
                conf["dir"] = allConfig?.getString("fileManagerListPath")
                conf["count"] = allConfig?.getInt("fileManagerListSize")
            }
        }
        conf["savePath"] = savePath
        return conf
    }

    private fun getArray(key: String): Array<String?> {
        val jsonArray = allConfig?.getJSONArray(key) ?: return arrayOfNulls(0)
        val result = arrayOfNulls<String>(jsonArray.length())
        var i = 0
        val len = jsonArray.length()
        while (i < len) {
            result[i] = jsonArray.getString(i)
            i++
        }
        return result
    }

    private fun readFile(inputStream: InputStream): String {
        val builder = StringBuilder()
        try {
            val reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
            val bfReader = BufferedReader(reader)
            var tmpContent: String?
            while (bfReader.readLine().also { tmpContent = it } != null) {
                builder.append(tmpContent)
            }
            bfReader.close()
        } catch (e: UnsupportedEncodingException) {
            // 忽略
        }
        return this.filter(builder.toString())
    }

    // 过滤输入字符串, 剔除多行注释以及替换掉反斜杠
    private fun filter(input: String): String {
        return input.replace("/\\*[\\s\\S]*?\\*/".toRegex(), "")
    }

    companion object {
        private const val configFileName = "config.json"

        // 涂鸦上传filename定义
        private const val SCRAWL_FILE_NAME = "scrawl"

        // 远程图片抓取filename定义
        private const val REMOTE_FILE_NAME = "remote"
        val instance: ConfigManager?
            /**
             * 配置管理器构造工厂
             *
             * @return 配置管理器实例或者null
             */
            get() = try {
                ConfigManager()
            } catch (e: Exception) {
                null
            }
    }
}
