package top.bettercode.summer.util.rapidauth

import top.bettercode.summer.util.rapidauth.entity.RapidauthResponse

/**
 *
 * @author Peter Wu
 */
interface IRapidauthClient {
    /**
     * @param carrier 运营商，移动：mobile， 联通：unicom，电信：telecom
     * @param token token 有效期为2分钟
     */
    fun query(carrier: String, token: String): RapidauthResponse

}