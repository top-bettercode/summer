package top.bettercode.summer.test

/**
 * @author Peter Wu
 */
interface AutoDocRequestHandler {
    fun handle(request: AutoDocHttpServletRequest)
    fun support(request: AutoDocHttpServletRequest): Boolean {
        return true
    }
}
