package top.bettercode.summer.util.mobile

import top.bettercode.summer.util.mobile.entity.QueryResponse

/**
 *
 * @author Peter Wu
 */
interface IMobileQueryClient {

    fun query(token: String): QueryResponse

}