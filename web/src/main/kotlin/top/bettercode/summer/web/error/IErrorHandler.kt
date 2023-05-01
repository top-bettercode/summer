package top.bettercode.summer.web.error

import top.bettercode.summer.web.RespEntity

/**
 * @author Peter Wu
 */
interface IErrorHandler {
    /**
     * 处理异常
     *
     * @param error      异常
     * @param respEntity 响应容器
     * @param errors     错误
     * @param separator  属性异常分隔符
     */
    fun handlerException(error: Throwable, respEntity: RespEntity<*>,
                         errors: MutableMap<String?, String?>, separator: String)

    /**
     * 得到国际化信息 未找到时返回代码 code
     *
     * @param code 模板
     * @param args 参数
     * @return 信息
     */
    fun getText(code: Any, vararg args: Any?): String
}
