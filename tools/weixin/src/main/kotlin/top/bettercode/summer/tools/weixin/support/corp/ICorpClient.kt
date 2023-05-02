package top.bettercode.summer.tools.weixin.support.corp

import top.bettercode.summer.tools.weixin.properties.ICorpProperties
import top.bettercode.summer.tools.weixin.support.corp.entity.CorpWebPageAccessToken

/**
 * 公众号接口
 *
 * @author Peter Wu
 */
interface ICorpClient {

    val properties: ICorpProperties

    fun getWebPageAccessToken(code: String): CorpWebPageAccessToken

    fun getWebPageAccessToken(code: String, retries: Int): CorpWebPageAccessToken

}