package cn.bestwu.autodoc.gen

/**
 * RequestLogging 处理
 *
 * @author Peter Wu
 */
interface AutoDocRequestHandler {

    fun handle(request: AutoDocHttpServletRequest)

}
