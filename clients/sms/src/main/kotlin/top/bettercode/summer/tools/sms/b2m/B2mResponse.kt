package top.bettercode.summer.tools.sms.b2m

import top.bettercode.summer.tools.lang.client.ClientResponse
import top.bettercode.summer.tools.lang.property.PropertiesSource.Companion.of

/**
 * @author Peter Wu
 */
class B2mResponse<T> : ClientResponse {
    //--------------------------------------------
    var code: String? = null
    var data: List<T>? = null

    constructor()
    constructor(data: List<T>?) {
        this.code = SUCCESS
        this.data = data
    }

    override val isOk: Boolean
        //--------------------------------------------
        get() = SUCCESS == code
    override val message: String
        get() = codeMessageSource.getOrDefault(code!!, code!!)

    companion object {
        const val SUCCESS = "SUCCESS"
        private val codeMessageSource = of("b2m-message")

        //--------------------------------------------
        fun getMessage(code: String?): String? {
            return code?.let { codeMessageSource.getOrDefault(code, it) }
        }
    }
}
