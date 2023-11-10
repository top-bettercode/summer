package top.bettercode.summer.test.autodoc

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.autodoc.AutodocExtension
import top.bettercode.summer.tools.generator.DataType

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
     * 数据源类型
     */
    var dataType = DataType.DATABASE

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
        private set

    fun setEntityPrefix(entityPrefix: String): GenProperties {
        this.entityPrefix = entityPrefix
        return this
    }
}
