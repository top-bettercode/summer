package top.bettercode.summer.util.test

import top.bettercode.api.sign.ApiSignAlgorithm
import top.bettercode.api.sign.ApiSignProperties

/**
 *
 * @author Peter Wu
 */
class AutoSignRequestHandler(
    private val apiSignProperties: ApiSignProperties,
    private val apiSignAlgorithm: ApiSignAlgorithm
) : AutoDocRequestHandler {

    override fun handle(request: AutoDocHttpServletRequest) {
        request.header(apiSignProperties.parameterName, apiSignAlgorithm.sign(request))
    }

}