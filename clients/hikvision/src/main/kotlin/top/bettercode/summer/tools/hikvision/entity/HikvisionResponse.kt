package top.bettercode.summer.tools.hikvision.entity

import top.bettercode.summer.tools.lang.client.ClientResponse

/**
 * 返回参数
 */
class HikvisionResponse<T> : ClientResponse {
    /**
     * 返回码，0-成功，其它参考附录E.2.1门禁管理错误码
     */
    var code: String? = null

    /**
     * 返回描述
     */
    override var message: String? = null

    /**
     * 返回数据
     */
    var data: T? = null

    override val isOk: Boolean
        get() = "0" == code
}
