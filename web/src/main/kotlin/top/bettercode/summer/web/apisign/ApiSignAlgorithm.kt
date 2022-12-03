package top.bettercode.summer.web.apisign

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.util.DigestUtils
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import top.bettercode.summer.web.apisign.ApiSignProperties
import javax.servlet.http.HttpServletRequest

/**
 * 签名算法

 * @author Peter Wu
 */
class ApiSignAlgorithm(val properties: ApiSignProperties) {

    val isSimple: Boolean = properties.isSimple

    /**
     * 验证参数签名

     * @param request 请求
     */
    fun checkSign(request: HttpServletRequest) {
        if (request.parameterMap.isEmpty() && isSimple)
            return

        val signParameter = properties.parameterName
        var sign: String? = request.getHeader(signParameter)
        if (sign == null) {
            sign = request.getParameter(signParameter)
        }

        if (skip(request)) {
            return
        }
        if (!StringUtils.hasText(sign)) {
            if (log.isWarnEnabled) {
                log.warn("客户端签名为空")
            }
            throw IllegalSignException()
        }
        if (sign!!.length != 32) {
            if (log.isWarnEnabled) {
                log.warn("客户端签名长度不匹配{}：{}", sign.length, sign)
            }
            throw IllegalSignException()
        }
        if (properties.isSimple) {
            val signParams = signParams(request)
            if (!sign.equals(signParams, ignoreCase = true)) {
                if (log.isWarnEnabled) {
                    log.warn(
                        "客户端参数签名错误,客户端：{}，服务端：{}，服务端待签名字段：{}",
                        sign,
                        signParams,
                        preSignStr(request)
                    )
                }
                throw IllegalSignException()
            }
        } else {
            val signParams = signParams(request).substring(0, 16)
            if (!sign.substring(0, 16).equals(signParams, ignoreCase = true)) {
                if (log.isWarnEnabled) {
                    log.warn(
                        "客户端参数签名错误,客户端：{}，服务端：{}，服务端待签名字段：{}",
                        sign.substring(0, 16),
                        signParams,
                        preSignStr(request)
                    )
                }
                throw IllegalSignException()
            }
            if (properties.isVerifyUserAgent) {
                val signUserAgent =
                    signUserAgent(request.getHeader(HttpHeaders.USER_AGENT)).substring(16, 24)
                if (!sign.substring(16, 24).equals(signUserAgent, ignoreCase = true)) {
                    if (log.isWarnEnabled) {
                        log.warn(
                            "客户端UserAgent签名错误,客户端：{}，服务端：{}",
                            sign.substring(16, 24),
                            signUserAgent
                        )
                    }
                    throw IllegalSignException()
                }
            }

            val clientTimeDifference = properties.allowableClientTimeDifference
            if (clientTimeDifference > 0) {
                val signTime = sign.substring(24, 32)
                val time = System.currentTimeMillis() / (clientTimeDifference * 1000)
                if (!signTime(time).substring(24, 32)
                        .equals(signTime, ignoreCase = true) && !signTime(time - 1)
                        .substring(24, 32).equals(signTime, ignoreCase = true) && !signTime(
                        time + 1
                    ).substring(24, 32).equals(signTime, ignoreCase = true)
                ) {
                    if (log.isWarnEnabled) {
                        log.warn("客户端时间签名错误,客户端：{}", sign.substring(24, 32))
                    }
                    throw IllegalSignException()
                }
            }
        }

        if (log.isDebugEnabled) {
            log.debug("签名验证通过")
        }
    }


    fun sign(request: HttpServletRequest): String {
        return if (properties.isSimple) {
            signParams(request)
        } else
            signParams(request).substring(
                0,
                16
            ) + signUserAgent(request.getHeader(HttpHeaders.USER_AGENT)).substring(
                16,
                24
            ) + signTime(
                System.currentTimeMillis() / properties.allowableClientTimeDifference
            ).substring(24, 32)
    }

    /**
     * @param requestParams 请求参数
     * *
     * @param userAgent userAgent
     * *
     * @return 签名
     */
    fun sign(requestParams: MultiValueMap<String, String>, userAgent: String = ""): String {
        return if (properties.isSimple) {
            signParams(requestParams)
        } else
            signParams(requestParams).substring(0, 16) + signUserAgent(userAgent).substring(
                16,
                24
            ) + signTime(
                System.currentTimeMillis() / properties.allowableClientTimeDifference
            ).substring(24, 32)
    }


    /**
     * 签名时间

     * @param time 时间
     * *
     * @return 签名后的时间
     */
    private fun signTime(time: Long): String {
        var timestamp = time.toString()
        timestamp += properties.clientSecret
        timestamp = DigestUtils.md5DigestAsHex(timestamp.toByteArray())
        return timestamp
    }

    /**
     * 签名 user-agent

     * @param userAgent userAgent
     * *
     * @return 签名后 user-agent
     */
    private fun signUserAgent(userAgent: String): String {
        var useragent = userAgent
        useragent += properties.clientSecret
        useragent = DigestUtils.md5DigestAsHex(useragent.toByteArray())
        return useragent
    }

    /**
     * @return 签名后的参数
     */
    private fun signParams(request: HttpServletRequest): String {
        val prestr = preSignStr(request)
        return DigestUtils.md5DigestAsHex(prestr.toString().toByteArray())
    }

    private fun preSignStr(request: HttpServletRequest): StringBuilder {
        val requestParams = request.parameterMap
        val keys = ArrayList(requestParams.keys)
        keys.sort()
        var prestr = StringBuilder("")
        for (key in keys) {
            val values = requestParams[key]
            val value = StringBuilder()
            val length = values!!.size
            for (i in 0 until length) {
                value.append(values[i] ?: "")
                value.append(if (i == length - 1) "" else ",")
            }
            if (value.toString() == "" || key.equals(
                    "sign",
                    ignoreCase = true
                ) || key.equals("sign_type", ignoreCase = true)
            ) {
                continue
            }
            prestr.append(key).append("=").append(value).append("&")
        }
        prestr = prestr.append(properties.clientSecret)
        if (log.isDebugEnabled) {
            log.debug("待签名参数字符串：{}", prestr)
        }
        return prestr
    }

    /**
     * @param requestParams 请求参数
     * *
     * @return 签名后的参数
     */
    private fun signParams(requestParams: MultiValueMap<String, String>): String {
        val keys = ArrayList(requestParams.keys)
        keys.sort()
        var prestr = StringBuilder("")
        for (key in keys) {
            val values = requestParams[key]
            val value = StringBuilder()
            val length = values!!.size
            for (i in 0 until length) {
                value.append(values[i])
                value.append(if (i == length - 1) "" else ",")
            }
            if (value.toString() == "" || key.equals(
                    "sign",
                    ignoreCase = true
                ) || key.equals("sign_type", ignoreCase = true)
            ) {
                continue
            }
            prestr.append(key).append("=").append(value).append("&")
        }
        if (log.isDebugEnabled) {
            log.debug("待签名参数字符串：{}", prestr)
        }
        prestr = prestr.append(properties.clientSecret)
        return DigestUtils.md5DigestAsHex(prestr.toString().toByteArray())
    }


    /**
     * @param request 请求
     * *
     * @return request 是否跳过
     */
    private fun skip(request: HttpServletRequest): Boolean {
        return if (properties.isCanSkip) {
            request.getAttribute("SKIP_SIGN") as? Boolean ?: false
        } else {
            false
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger(ApiSignAlgorithm::class.java)

        /**
         * 过滤签名验证
         */
        fun skip() {
            request!!.setAttribute("SKIP_SIGN", true)
        }

        /**
         * @return 当前请求
         */
        private val request: HttpServletRequest?
            get() {
                val requestAttributes = RequestContextHolder
                    .getRequestAttributes() as? ServletRequestAttributes
                return requestAttributes?.request
            }
    }

}

