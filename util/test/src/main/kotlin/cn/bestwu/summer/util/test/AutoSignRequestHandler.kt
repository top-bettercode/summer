package cn.bestwu.summer.util.test

import cn.bestwu.api.sign.ApiSignAlgorithm
import cn.bestwu.api.sign.ApiSignProperties

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