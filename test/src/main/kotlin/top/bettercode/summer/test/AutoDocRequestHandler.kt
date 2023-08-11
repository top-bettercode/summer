package top.bettercode.summer.test

/**
 * @author Peter Wu
 */
interface AutoDocRequestHandler {
    fun handle(request: AutoDocHttpServletRequest)
    @JvmDefault
    fun support(request: AutoDocHttpServletRequest): Boolean {
        return true
    }
}
