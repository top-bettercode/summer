package top.bettercode.summer.web.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.spring.jackson")
open class JacksonExtProperties {
    /**
     * xml root name.
     */
    var xmlRootName = "xml"

    /**
     * Feature that controls whether XML declaration should be written before when generator is
     * initialized (true) or not (false).
     */
    var isWriteXmlDeclaration = false

    /**
     * Feature that controls whether null should be serialize as empty.
     */
    var isDefaultEmpty = true

    /**
     * Feature that controls whether null object should be serialized as empty.
     */
    var isNullObjectAsEmpty = false

    /**
     * specifying the packages to scan for mixIn annotation.
     */
    var mixInAnnotationBasePackages = arrayOf<String>()
}
