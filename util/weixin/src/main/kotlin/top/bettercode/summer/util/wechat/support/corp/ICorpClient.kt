package top.bettercode.summer.util.wechat.support.corp

import top.bettercode.summer.util.wechat.config.ICorpProperties
import top.bettercode.summer.util.wechat.support.corp.entity.CorpWebPageAccessToken

/**
 * 公众号接口
 *
 * @author Peter Wu
 */
interface ICorpClient {

    val properties: ICorpProperties

    fun getWebPageAccessToken(code: String): CorpWebPageAccessToken

}