package top.bettercode.summer.test

import top.bettercode.summer.apisign.ApiSignAlgorithm
import top.bettercode.summer.apisign.ApiSignProperties

/**
 *
 * @author Peter Wu
 */
class AutoSignRequestHandler(
    private val apiSignProperties: ApiSignProperties
) : AutoDocRequestHandler {
    private val apiSignAlgorithm: ApiSignAlgorithm = ApiSignAlgorithm(apiSignProperties)

    override fun handle(request: AutoDocHttpServletRequest) {
        request.header(apiSignProperties.parameterName, apiSignAlgorithm.sign(request))
    }

}