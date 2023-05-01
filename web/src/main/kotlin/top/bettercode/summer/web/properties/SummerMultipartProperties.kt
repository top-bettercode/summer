package top.bettercode.summer.web.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.multipart")
class SummerMultipartProperties {
    //--------------------------------------------
    /**
     * 文件保存路径
     */
    var baseSavePath: String? = null

    /**
     * 文件访问路径前缀
     */
    var fileUrlFormat: String? = null

    /**
     * 保留原文件名
     */
    var isKeepOriginalFilename = false

    /**
     * 默认文件分类
     */
    var defaultFileType = "file"

    /**
     * 文件资源访问位置
     */
    var staticLocations: Array<String> = arrayOf()
        get() = if (field.isNotEmpty()) field else arrayOf(
                "file:" + if (baseSavePath!!.endsWith("static")) baseSavePath!!.substringBeforeLast("static", baseSavePath!!) else baseSavePath)
}
