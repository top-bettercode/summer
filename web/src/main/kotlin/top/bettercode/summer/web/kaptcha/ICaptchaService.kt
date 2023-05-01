package top.bettercode.summer.web.kaptcha

/**
 * @author Peter Wu
 */
interface ICaptchaService {
    /**
     * @param loginId 客户端ID
     * @param text    客户端验证码
     */
    fun save(loginId: String?, text: String?)

    /**
     * @param loginId 客户端ID
     * @param text    客户端验证码
     * @return 是否匹配
     */
    fun match(loginId: String?, text: String): Boolean
}
