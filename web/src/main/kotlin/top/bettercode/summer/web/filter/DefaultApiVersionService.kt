package top.bettercode.summer.web.filter

import top.bettercode.summer.web.properties.SummerWebProperties

/**
 * @author Peter Wu
 */
class DefaultApiVersionService(private val summerWebProperties: SummerWebProperties) : IApiVersionService {
    override val versionName: String?
        get() = summerWebProperties.versionName
    override val version: String?
        get() = summerWebProperties.version
    override val versionNoName: String?
        get() = summerWebProperties.versionNoName
    override val versionNo: String?
        get() = summerWebProperties.versionNo
}
