package top.bettercode.summer.tools.mobile

import top.bettercode.summer.tools.mobile.entity.QueryResponse

/**
 *
 * @author Peter Wu
 */
interface IMobileQueryClient {

    fun query(token: String): QueryResponse

}