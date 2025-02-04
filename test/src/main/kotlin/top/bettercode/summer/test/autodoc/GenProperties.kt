package top.bettercode.summer.test.autodoc

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.autodoc.AutodocExtension

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.autodoc.gen")
open class GenProperties : AutodocExtension() {
    /**
     * 是否启用
     */
    var isEnable = true

    /**
     * 异常时是否不生成文档
     */
    var isDisableOnException = true

    /**
     * 项目路径
     */
    var projectPath = ""

    /**
     * 表前缀
     */
    var tablePrefixes = arrayOf<String>()

    /**
     * 表后缀
     */
    var tableSuffixes = arrayOf<String>()

    /**
     * 忽略请求头参数
     */
    var ignoredHeaders = arrayOf<String>()

    /**
     * 实体前缀
     */
    var entityPrefix = ""

}
